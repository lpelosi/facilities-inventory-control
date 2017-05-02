/* 
Author: Louis Pelosi, Anthony D'Orazio 
Designed by Louis Pelosi
Images used under Creative Commons License
*/
package ordertracker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javafx.scene.text.Font;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.sort;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.OrientationRequested;

public class OrderTracker extends Application {

        private final String stringConfigDatabase = "facilities", 
                databaseDriverString = "com.mysql.jdbc.Driver",
                connectionString = "jdbc:mysql://localhost/";
        private String stringConfigDBUsername, 
                stringConfigDBPassword;
        private Connection connection = null;
        private TextField tfOrderNumber = new TextField("");
        private TextField tfOrderDate = new TextField("");
        private TextField tfItemName = new TextField("");
        private TextField tfItemNumber = new TextField("");
        private TextField tfQuantity = new TextField("");
        private TextField tfLocation = new TextField("");
        private TextField tfItemPrice = new TextField("");
        private TextField tfItemSupplier = new TextField("");
        private TextField tfInvoice = new TextField("");
        private String tableID = "", searchBy = "", itemToSearchBy = "";
        private TextArea taOrderDetail = new TextArea();
        
        //ICONS
        
        //MENU LAYOUT
        private final MenuItem menuExit = new MenuItem("Exit");
        private final MenuBar menuBar = new MenuBar();
        private final MenuItem menuAddUser = new MenuItem("Add User");
        private final Menu menuFile = new Menu("File");
        private final Menu menuItemSearch = new Menu("Search by Item");
        private final MenuItem mainInvItem = new MenuItem("Maintenance Inv");
        private final MenuItem mainInvOrder = new MenuItem(
                "Maintenance Orders");
        private final MenuItem hkInvItem = new MenuItem("Housekeeping Inv");
        private final MenuItem hkInvOrder = new MenuItem("Housekeeping Orders");
        private final Menu menuHelp = new Menu("Help");
        private final MenuItem menuAbout = new MenuItem("About");
        private final MenuItem howToUse = new MenuItem("How to use");
        private final Menu menuOrderSearch = new Menu("Search by Order");
        private final Menu addToDatabase = new Menu("Manage database");
        private final MenuItem addToMain = new MenuItem("Manage Maintenance");
        private final MenuItem addToHk = new MenuItem("Manage Housekeeping");
        private final MenuItem searchSuppliers = new MenuItem(
                "Search supplier contacts");
        
        //DROP DOWN ITEMS
        private final ComboBox orderNumberBox = new ComboBox();
        private final ComboBox dateBox = new ComboBox();
        private final ComboBox itemsBox = new ComboBox();
        
    @Override
    public void start(Stage primaryStage){
        loginScreen();
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root);
        
        menuAddUser.setOnAction(e->{
            addUser();
        });
        
        searchSuppliers.setOnAction(e->{
            primaryStage.setScene(supplierInfoScene());
        });
        
        menuExit.setOnAction((ActionEvent e)-> {
            Alert exitConfirm = new Alert(Alert.AlertType.CONFIRMATION);
            exitConfirm.setGraphic(new ImageView(this.getClass().getResource(
            "questionSmall.png").toString()));
            exitConfirm.setTitle("Exit confirmation");
            exitConfirm.setHeaderText(null);
            exitConfirm.setContentText("Are you sure you want to exit?");

            Optional<ButtonType> result = exitConfirm.showAndWait();
            if(result.get() == ButtonType.OK){
                System.exit(0);
            }
        });

        mainInvItem.setOnAction((ActionEvent e)->{
            tableID = "main_inventory";
            primaryStage.setScene(initSearchScene());
        });
        hkInvItem.setOnAction((ActionEvent e)->{
            tableID = "hk_inventory";
            primaryStage.setScene(initSearchScene());
        });
        
        mainInvOrder.setOnAction((ActionEvent e)->{
            tableID = "main_inventory";
            primaryStage.setScene(displayOrdersScene());
        });
        hkInvOrder.setOnAction((ActionEvent e)->{
            tableID = "hk_inventory";
            primaryStage.setScene(displayOrdersScene());
        });
        
        addToMain.setOnAction(e->{
           tableID = "main_inventory";
           primaryStage.setScene(updateOrdersScene());
        });
        
        addToHk.setOnAction(e->{
           tableID = "hk_inventory";
           primaryStage.setScene(updateOrdersScene());
        });

        howToUse.setOnAction((ActionEvent e)->{
            Alert howToAlert = new Alert(Alert.AlertType.INFORMATION);
            howToAlert.setGraphic(new ImageView(this.getClass().getResource(
            "infoSmall.png").toString()));
           howToAlert.setHeaderText(null);
           howToAlert.setContentText("Select from the menu to search by item "
           +"or order, then select which inventory to use. You can also add"
                   + " orders to the database and edit previously added orders."
                   + " To create a new user you must have administrative rights"
                   + ", non admin users will be unable to create new users.");

           howToAlert.showAndWait();
        });
        
        menuAbout.setOnAction((ActionEvent e)->{
           Alert aboutAlert = new Alert(AlertType.INFORMATION);
           aboutAlert.setGraphic(new ImageView(this.getClass().getResource(
            "logoSmall.png").toString()));
           aboutAlert.setTitle("Credits");
           aboutAlert.setHeaderText("orderTracker v1.0");
           String credits = "Written and designed by Anthony D'Orazio"+
                   " and Louis Pelosi. \n"
                   + "Icon designs used under creative commons"
           + " license https://creativecommons.org/licenses/by-nd/3.0/";
           aboutAlert.setContentText(credits);
           
           aboutAlert.showAndWait();
        });

        menuFile.getItems().addAll(menuAddUser,searchSuppliers,menuExit);
        addToDatabase.getItems().addAll(addToMain,addToHk);
        menuHelp.getItems().addAll(howToUse,menuAbout);
        menuItemSearch.getItems().addAll(mainInvItem,hkInvItem);
        menuOrderSearch.getItems().addAll(mainInvOrder,hkInvOrder);
        menuBar.getMenus().addAll(menuFile,menuItemSearch,menuOrderSearch,
                addToDatabase,menuHelp);
        root.setTop(menuBar);
        
        primaryStage.setScene(scene);
        
        primaryStage.setTitle("Order Tracker");
        primaryStage.getIcons().add(new Image(this.getClass()
                .getResource("logoSmall.png").toString()));
        primaryStage.setHeight(550);
        primaryStage.setWidth(1000);
        primaryStage.show();
    }
    
    // METHOD FOR OBTAINING USER LOGIN CREDENTIALS FOR DATABASE ACCESS
    private void loginScreen(){
        Dialog<Pair<String,String>> loginScreen = new Dialog<>();
        loginScreen.setHeaderText("Please login to continue.");
        ButtonType loginButtonType = new ButtonType("Login", 
                ButtonData.OK_DONE);
        loginScreen.getDialogPane().getButtonTypes().addAll(loginButtonType,
                ButtonType.CANCEL);
        
        GridPane loginGrid = new GridPane();
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(20,150,10,10));
        
        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        
        loginGrid.add(new Label("Username: "),0,0);
        loginGrid.add(username,1,0);
        loginGrid.add(new Label("Password: "),0,1);
        loginGrid.add(password,1,1);
        
        Node loginButton = loginScreen.getDialogPane()
                .lookupButton(loginButtonType);
        loginButton.setDisable(true);
        
        username.textProperty().addListener((observable, oldValue, newValue)->{
            loginButton.setDisable(newValue.trim().isEmpty());
        });
        
        loginScreen.getDialogPane().setContent(loginGrid);
        
        Platform.runLater(()-> username.requestFocus());
        
        loginScreen.setResultConverter(dialogButton->{
            if(dialogButton == loginButtonType){
                return new Pair<>(username.getText(),password.getText());
            }
            return null;
        });
        
        Optional<Pair<String,String>> result = loginScreen.showAndWait();
        
        result.ifPresent((usernamePassword->{
            stringConfigDBUsername = usernamePassword.getKey();
            stringConfigDBPassword = usernamePassword.getValue();
        }));
            initializeDB();
    }

    // METHOD FOR CONNECTING TO DATABASE
    private void initializeDB(){
        try{
            Class.forName(databaseDriverString);
            System.out.println("Driver Loaded.");

            connection = DriverManager.getConnection
            (connectionString + stringConfigDatabase, stringConfigDBUsername,
            stringConfigDBPassword);
            System.out.println("Database Connected.");
      
        }catch(ClassNotFoundException | SQLException ex) {
      
        }
    }

    // METHOD TO ADD USER TO DATABASE AND APP
    private void addUser(){
        Dialog<Pair<String,String>> addUserScreen = new Dialog<>();
        addUserScreen.setHeaderText("Add a user with full access to database.");
        ButtonType addButtonType = new ButtonType("Add User", ButtonData.OK_DONE);
        addUserScreen.getDialogPane().getButtonTypes().addAll(addButtonType,
                ButtonType.CANCEL);
        Node addButton = addUserScreen.getDialogPane()
                .lookupButton(addButtonType);
        addButton.setDisable(true);
        
        
        GridPane addPane = new GridPane();
        addPane.setHgap(10);
        addPane.setVgap(10);
        addPane.setPadding(new Insets(20,150,10,10));
        
        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        
        Button checkName = new Button("Check username");
        
        username.textProperty().addListener((observable,oldValue,newValue)->{
            checkName.setDisable(newValue.trim().isEmpty());
        });
        
        checkName.setOnAction(e->{
            try{
            PreparedStatement tableChange = connection
                    .prepareStatement("use mysql;");
            tableChange.executeQuery();
            
            Statement userCheckStatement = connection.createStatement();
            ResultSet rSetUser = userCheckStatement.executeQuery(
                    "select user from user where user='"+username.getText()
                            + "';");
            System.out.println("select user from user where user='"+
                    username.getText() + "';");
            if(!rSetUser.next()){
                addButton.setDisable(false);
                
            }else{
                Alert nameTaken = new Alert(AlertType.ERROR);
                nameTaken.setHeaderText("Error.");
                nameTaken.setContentText("User name is already in use.");
                addButton.setDisable(true);
                nameTaken.showAndWait();
            }
            
            }catch(SQLException ex){
                
            }
        });
        
        addPane.add(new Label("Username: "),0,0);
        addPane.add(username,1,0);
        addPane.add(checkName,2,0);
        addPane.add(new Label("Password: "),0,1);
        addPane.add(password, 1, 1);
        
        addUserScreen.getDialogPane().setContent(addPane);
        
        Platform.runLater(()-> username.requestFocus());
        
        addUserScreen.setResultConverter(dialogButton->{
            if(dialogButton == addButtonType){
                return new Pair<>(username.getText(),password.getText());
            }
            return null;
        });
        
        Optional<Pair<String,String>> result = addUserScreen.showAndWait();
        
        result.ifPresent((usernamePassword->{
            String usernameCheck = usernamePassword.getKey();
            String passwordCheck = usernamePassword.getValue();
            String addUserString = "create user '" + usernameCheck + 
                    "'@'localhost' identified by '" + passwordCheck + "';";
            String grantPrivs = 
                    "grant select,insert,update,delete on facilities.* to '" +
                    usernameCheck + "'@'localhost';";
            try{
                PreparedStatement addUserState = connection
                        .prepareStatement(addUserString);
                addUserState.execute();
                PreparedStatement grantPrivState = connection
                        .prepareStatement(grantPrivs);
                grantPrivState.execute();
            
            }catch(SQLException ex){
                
            }
            
        }));
    }
    // METHOD FOR SETTING TEXTFIELDS
    private void setInventoryFields() throws Exception {
        ResultSet resultSetOfInventory = null;
        Statement statementForInventory = null;
        
        try {
            statementForInventory = connection.createStatement();
            resultSetOfInventory = statementForInventory.executeQuery(
                "SELECT * FROM " +tableID+" WHERE "+ searchBy + " = '" + 
                        itemToSearchBy+"'");
            while(resultSetOfInventory.next()){
                tfOrderNumber.setText(resultSetOfInventory.getObject("ordernumber").toString());
                tfOrderDate.setText(resultSetOfInventory.getObject("orderdate").toString());
                tfItemNumber.setText(resultSetOfInventory.getObject("itemnumber").toString());
                tfItemName.setText(resultSetOfInventory.getObject("item").toString());
                tfQuantity.setText(resultSetOfInventory.getObject("quantity").toString());
                tfItemPrice.setText(resultSetOfInventory.getObject("price").toString());
                tfLocation.setText(resultSetOfInventory.getObject("location").toString());
                tfItemSupplier.setText(resultSetOfInventory.getObject("supplier").toString());
                tfInvoice.setText(resultSetOfInventory.getObject("invoice").toString());
            }
            
        }
        catch (SQLException ex){
        }
    }
    
    // GUI SCENE FOR DISPLAYING ENTIRE ORDERS AND SEARCHING ORDERS
    private Scene displayOrdersScene(){
        BorderPane ordersPane = new BorderPane();
        GridPane fieldsPane = new GridPane();
        VBox searchDisplay = new VBox();
        HBox orderSearch = new HBox();
        ArrayList<String> dateList = new ArrayList<>();
        ArrayList<String> orderNumberList = new ArrayList<>();
        Button printTextArea = new Button("Print results");
        
        printTextArea.setOnAction(e->{
            try {
                print(taOrderDetail.getText());
            } catch (PrintException ex) {
                Logger.getLogger(OrderTracker.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(OrderTracker.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        try{
            Statement statementForOrderNumber = connection.createStatement();
            ResultSet rSetOfNumbers = statementForOrderNumber.executeQuery(
                    "select ordernumber from " + tableID + ";");
            
            while(rSetOfNumbers.next()){
                String orderNumber = rSetOfNumbers.
                        getObject("ordernumber").toString();
                if(orderNumberList.contains(orderNumber)){
                    
                }else{
                    orderNumberList.add(orderNumber);
                }
            }
            sort(orderNumberList);
            ObservableList<String> orderNumberBoxList = FXCollections.
                    observableArrayList(orderNumberList);
            orderNumberBox.setItems(orderNumberBoxList);
            
            Statement statementForDates = connection.createStatement();
            ResultSet rSetForDates = statementForDates.executeQuery(
                    "select orderdate from " + tableID + ";");
            while(rSetForDates.next()){
                String orderDate = rSetForDates.getObject("orderdate")
                        .toString();
                if(dateList.contains(orderDate)){
                    
                }else{
                    dateList.add(orderDate);
                }
                
            }
            sort(dateList);
            Collections.reverse(dateList);
            ObservableList<String> dateBoxList = FXCollections.
                    observableArrayList(dateList);
            dateBox.setItems(dateBoxList);
            
            Statement itemBoxCombo = connection.createStatement();
            ResultSet rSetForItems = itemBoxCombo.executeQuery(
                "select item from " + tableID + ";");
            ArrayList<String> itemsList = new ArrayList<>();
            
            while(rSetForItems.next()){
                String item = rSetForItems.getObject("item").toString();
                if(itemsList.contains(item)){
                
                }else{
                    itemsList.add(item);
                }
            }
            sort(itemsList);
            ObservableList<String> itemBoxList = FXCollections.
                    observableArrayList(itemsList);
            itemsBox.setItems(itemBoxList);
            
        }catch(SQLException ex){
            
        }
        
        Button numberButton = new Button("Search by number");
        numberButton.setOnAction(e->{
            try {
                itemToSearchBy = "ordernumber";
                setInventoryFields();
                displayOrdersForAll("ordernumber = '" + orderNumberBox.
                        getValue().toString()+"';");
                tfOrderNumber.setText(orderNumberBox.getValue().toString());
            } catch (Exception ex) {
                Logger.getLogger(OrderTracker.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        });
        
        Button dateButton = new Button("Search by date");
        dateButton.setOnAction(e->{
            try {
                itemToSearchBy = "orderdate";
                setInventoryFields();
                displayOrdersForAll("orderdate = '" + dateBox.getValue()
                        .toString() +"';");
                tfOrderDate.setText(dateBox.getValue().toString());
            } catch (Exception ex) {
                Logger.getLogger(OrderTracker.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        });
        
        Button itemButton = new Button("Search by item");
        itemButton.setOnAction(e->{
            try {
                itemToSearchBy = "item";
                setInventoryFields();
                displayOrdersForAll("item = '" + itemsBox.getValue().toString()
                        +"';");
                tfItemName.setText(itemsBox.getValue().toString());
            } catch (Exception ex) {
                Logger.getLogger(OrderTracker.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        });
        
        Button itemNButton = new Button("Search by item number");
        itemNButton.setOnAction(e->{
            try {
                itemToSearchBy = "itemnumber";
                setInventoryFields();
                displayOrdersForAll("itemnumber = '" + tfItemNumber
                        .getText()+"';");
            } catch (Exception ex) {
                Logger.getLogger(OrderTracker.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        });
        
        Button locationButton = new Button("Search by location");
        locationButton.setOnAction(e->{
            try {
                itemToSearchBy = "location";
                setInventoryFields();
                displayOrdersForAll("location = '" + tfLocation.getText()+"';");
            } catch (Exception ex) {
                Logger.getLogger(OrderTracker.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        });
        
        Button supplierButton = new Button("Search by supplier");
        supplierButton.setOnAction(e->{
            try {
                itemToSearchBy = "supplier";
                setInventoryFields();
                displayOrdersForAll("supplier = '" + tfItemSupplier
                        .getText()+"';");
            } catch (Exception ex) {
                Logger.getLogger(OrderTracker.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        });
        
        Button invoiceButton = new Button("Search by invoice");
        invoiceButton.setOnAction(e->{
            try {
                itemToSearchBy = "invoice";
                setInventoryFields();
                displayOrdersForAll("invoice = '" + tfInvoice.getText()+"';");
            } catch (Exception ex) {
                Logger.getLogger(OrderTracker.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        });
        
        Button clearButton = new Button("Clear all fields");
        clearButton.setOnAction(r->{
           clearFields(); 
        });
        
        fieldsPane.add(new Label("Order Number : "),0,0);
        fieldsPane.add(orderNumberBox,1,0);
        fieldsPane.add(numberButton,2,0);
        fieldsPane.add(clearButton,3,0);
        fieldsPane.add(new Label("Order Date : "),0,1);
        fieldsPane.add(dateBox, 1, 1);
        fieldsPane.add(dateButton,2,1);
        fieldsPane.add(new Label("Item Name : "),0,2);
        fieldsPane.add(itemsBox,1,2);
        fieldsPane.add(itemButton,2,2);
        fieldsPane.add(new Label("Item Number : "),0,3);
        fieldsPane.add(tfItemNumber,1,3);
        fieldsPane.add(itemNButton,2,3);
        fieldsPane.add(new Label("Item Quantity : "),0,4);
        fieldsPane.add(tfQuantity,1,4);
        fieldsPane.add(new Label("Item Location : "),0,5);
        fieldsPane.add(tfLocation,1,5);
        fieldsPane.add(locationButton,2,5);
        fieldsPane.add(new Label("Cost : "),0,6);
        fieldsPane.add(tfItemPrice,1,6);
        fieldsPane.add(new Label("Supplier : "),0,7);
        fieldsPane.add(tfItemSupplier,1,7);
        fieldsPane.add(supplierButton,2,7);
        fieldsPane.add(new Label("Invoice Number : "),0,8);
        fieldsPane.add(tfInvoice,1,8);
        fieldsPane.add(invoiceButton,2,8);
        fieldsPane.add(printTextArea,3,8);
        
        Label lbDatabase = new Label();
        
        if(tableID.equals("main_inventory")){
            lbDatabase.setText("*** Using Maintenance Database ***");
        } else if(tableID.equals("hk_inventory")){
            lbDatabase.setText("*** Using Housekeeping Database ***");
        }
        
        taOrderDetail.setFont(new Font("Courier", 16));
        
        searchDisplay.getChildren().addAll(fieldsPane,
                taOrderDetail,lbDatabase);
        
        orderSearch.setPadding(new Insets(10,10,10,10));
        searchDisplay.setPadding(new Insets(10,10,10,10));
        
        ordersPane.setTop(menuBar);
        ordersPane.setCenter(searchDisplay);
        return new Scene(ordersPane);
    }
    
    // METHOD TO COLLECT ORDER INFO AND DISPLAY IN TEXTAREA
    private void displayOrdersForAll(String searchCondition){
        String orderDetailsLine = "", orderDetailsList = "", orderNumber = "", 
                orderDate = "", item = "", itemNumber = "", location = "", 
                quantity = "", price = "", supplier = "", invoice = "";
        Statement statementToSearch = null;
        ResultSet rSetSearch = null;
        
        String labelDescriptor = String.format(
        "%-15s%3s%-10s%3s%1s%3s%-50s%3s%-15s%3s%-7s%3s%-8s%3s%-24s%3s%-15s",
                "Order Number"," | ","Order Date"," | ","X"," | ",
                "Item"," | ","ItemNumber"," | ","Price"," | ","Quantity",
                " | ","Supplier"," | ","Invoice");
        
        orderDetailsList += (labelDescriptor + "\n");
        
        try{
            statementToSearch = connection.createStatement();
            rSetSearch = statementToSearch.executeQuery("select * from " +
                    tableID + " where " + searchCondition);
            
            while(rSetSearch.next()){
                orderNumber = rSetSearch.getObject("ordernumber").toString()
                        .trim();
                orderDate = rSetSearch.getObject("orderdate").toString().trim();
                supplier = rSetSearch.getObject("supplier").toString().trim();
                location = rSetSearch.getObject("location").toString().trim();
                item = rSetSearch.getObject("item").toString().trim();
                itemNumber = rSetSearch.getObject("itemnumber").toString()
                        .trim();
                price = rSetSearch.getObject("price").toString().trim();
                quantity = rSetSearch.getObject("quantity").toString().trim();
                invoice = rSetSearch.getObject("invoice").toString().trim();
                
                tfOrderNumber.setText(orderNumber);
                tfOrderDate.setText(orderDate);
                tfInvoice.setText(invoice);
                tfItemSupplier.setText(supplier);
                
                orderDetailsLine = String.format(
          "%-15s%3s%-10s%3s%1s%3s%-50s%3s%-15s%3s%-7s%3s%-8s%3s%-24s%3s%-15s",
                        orderNumber," | ",orderDate," | ",location," | ",item,
                        " | ",itemNumber," | ",price," | ",quantity," | ",
                        supplier," | ",invoice);
                
                orderDetailsList += (orderDetailsLine + "\n");
                
            }
            
        }catch(SQLException ex){
            
        }
        taOrderDetail.setText(orderDetailsList);
    }
    
    // GUI SCENE TO SEARCH BY ITEM NAME/NUMBER
    private Scene initSearchScene(){
        BorderPane root = new BorderPane();
        GridPane invPane = new GridPane();
        invPane.setPadding(new Insets(5,5,5,5));
        
        Button searchByName = new Button("Search by Name");
        Button searchByItemNumber = new Button("Search by Item Number");
        Button clearButton = new Button("Clear all fields");
        
        try{
            Statement itemBoxCombo = connection.createStatement();
            ResultSet rSetForItems = itemBoxCombo.executeQuery(
                "select item from " + tableID + ";");
            ArrayList<String> itemsList = new ArrayList<>();
            
            while(rSetForItems.next()){
                String item = rSetForItems.getObject("item").toString();
                if(itemsList.contains(item)){
                
                }else{
                    itemsList.add(item);
                }
            }
            sort(itemsList);
            ObservableList<String> itemBoxList = FXCollections.
                    observableArrayList(itemsList);
            itemsBox.setItems(itemBoxList);
        }catch(SQLException ex){
            
        }
        
        clearButton.setOnAction(e->{
           clearFields(); 
        });
        
        searchByItemNumber.setOnAction(r->{
            searchBy = "itemnumber";
            itemToSearchBy = tfItemNumber.getText();
            try {
                setInventoryFields();
            } catch (Exception ex) {
            }
        });
        
        searchByName.setOnAction(r->{
            searchBy = "item";
            itemToSearchBy = itemsBox.getValue().toString();
            try{
                setInventoryFields();
            } catch(Exception ex){
            }
        });
        
        HBox buttonBox = new HBox();
        buttonBox.getChildren().addAll(searchByName,searchByItemNumber);
        
        Label lbDatabase= new Label();
        
        if(tableID.equals("main_inventory")){
            lbDatabase.setText("*** Using Maintenance Database ***");
        } else if(tableID.equals("hk_inventory")){
            lbDatabase.setText("*** Using Housekeeping Database ***");
        }
        
        invPane.add(new Label("Item Name : "),0,0);
        invPane.add(itemsBox,1,0);
        invPane.add(clearButton,2,0);
        invPane.add(new Label("Item Number : "),0,1);
        invPane.add(tfItemNumber,1,1);
        invPane.add(new Label("Cost : "),0,2);
        invPane.add(tfItemPrice,1,2);
        invPane.add(new Label("Supplier : "),0,3);
        invPane.add(tfItemSupplier,1,3);
        
        VBox gridAndButton = new VBox();
        gridAndButton.setPadding(new Insets(10,10,10,10));
        
        gridAndButton.getChildren().addAll(invPane, buttonBox, lbDatabase);
        
        root.setTop(menuBar);
        root.setCenter(gridAndButton);
        
        return new Scene(root,300,250);
    }
    
    // GUI SCENE FOR ADDING/EDITING DATABASE
    private Scene updateOrdersScene(){
        BorderPane root = new BorderPane();
        GridPane fieldsPane = new GridPane();
        HBox userGUI = new HBox();
        GridPane buttonBox = new GridPane();
        VBox fullScene = new VBox();
        Insets padding = new Insets(10,10,10,10);
        Label status = new Label();
        status.setPadding(padding);
        Image denyImage = new Image(getClass()
                .getResourceAsStream("denySmall.png"));
        Image upImage = new Image(getClass()
                .getResourceAsStream("upSmall.png"));
        Image plusImage = new Image(getClass()
                .getResourceAsStream("plusSmall.png"));
        Image newImage = new Image(getClass()
                .getResourceAsStream("newSmall.png"));
        
        Button clearAllButton = new Button("Clear all fields");
        Button deleteOrderButton = new Button("Delete order number", 
            new ImageView(denyImage));
        Button deleteItemButton = new Button("Delete Item", 
            new ImageView(denyImage));
        Button newItemButton = new Button("New item", 
            new ImageView(newImage));
        Button addToDatabaseButton = new Button("Add to database", 
            new ImageView(plusImage));
        Button updateDatabase = new Button("Update entry by order number", 
            new ImageView(upImage));
        Button updateOrderNumber = new Button("Update order number by invoice", 
            new ImageView(upImage));
        Button sameSupplier = new Button("New order same supplier", 
            new ImageView(newImage));
        
        clearAllButton.setOnAction(e->{
           clearFields(); 
        });
        
        deleteItemButton.setOnAction(e->{
            String deleteItemString = "DELETE FROM " + tableID + " WHERE " +
                    "ordernumber = '" + tfOrderNumber.getText() + "' AND " +
                    "item = '" + tfItemName.getText() + "';";
            try {
                PreparedStatement deleteItemStatement = connection.
                        prepareStatement(deleteItemString);
                deleteItemStatement.execute();
                status.setText("Deletion Successful");
            } catch (SQLException ex) {
                Logger.getLogger(OrderTracker.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        });
        
        deleteOrderButton.setOnAction(e->{
            try {
                PreparedStatement deleteStatement = null;
                String  deleteString = "DELETE FROM " + tableID + " WHERE " +
                        "ordernumber = '" + tfOrderNumber.getText() + "';";
                deleteStatement = connection.prepareStatement(deleteString);
                deleteStatement.execute();
                status.setText("Deletion Successful.");
            } catch (SQLException ex) {
                status.setText("Deletion failed.");
                Logger.getLogger(OrderTracker.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        });
        
        newItemButton.setOnAction(e->{
           tfItemName.setText("");
           tfItemNumber.setText("");
           tfQuantity.setText("");
           tfItemPrice.setText("");
           tfItemName.requestFocus();
        });
        
        sameSupplier.setOnAction(e->{
            tfItemName.setText("");
           tfItemNumber.setText("");
           tfQuantity.setText("");
           tfLocation.setText("");
           tfItemPrice.setText("");
           tfOrderNumber.setText("");
           tfOrderDate.setText("");
           tfInvoice.setText("");
        });
        
        addToDatabaseButton.setOnAction(e->{
            try {
                PreparedStatement addToStatement = null; 
                String prepStatement = "INSERT INTO " + tableID +
                        " (orderdate,ordernumber,location,item,"
                        + "itemnumber,price,"
                        + "quantity,supplier,invoice) VALUES ('" + 
                        tfOrderDate.getText().toUpperCase() +"','"
                        + tfOrderNumber.getText().toUpperCase() + 
                        "','" + tfLocation.getText().toUpperCase() +" ','"
                        + tfItemName.getText().toUpperCase() + 
                        "','" + tfItemNumber.getText().toUpperCase() + 
                        "','" + tfItemPrice.getText() + "','" + 
                        tfQuantity.getText() + "','" + 
                        tfItemSupplier.getText().toUpperCase()
                        + "','" + tfInvoice.getText().toUpperCase() + "');";
                
                addToStatement = connection.prepareStatement(prepStatement);
                addToStatement.execute();
                
            } catch (SQLException ex) {
                Logger.getLogger(OrderTracker.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        });
        
        updateDatabase.setOnAction(e->{
            try{
                String updateString = "UPDATE " + tableID +  
                        " SET orderdate = '" + tfOrderDate.getText() + 
                        "', location = '" + tfLocation.getText().toUpperCase()
                        + "', " + "item = '" + tfItemName.getText()
                                .toUpperCase() +
                        "', itemnumber = '" + tfItemNumber.getText()
                                .toUpperCase() + 
                        "', price = '" + tfItemPrice.getText() + "', " +
                        "quantity = '" + tfQuantity.getText() + "', supplier = '"
                        + tfItemSupplier.getText().toUpperCase()
                        + "', invoice = '" + tfInvoice.getText().toUpperCase()
                        + "' WHERE ordernumber = '" + tfOrderNumber.getText() 
                        + "';";
                System.out.println(updateString);
                PreparedStatement updateStatement = connection
                        .prepareStatement(updateString);
                
                updateStatement.execute();
                
            }catch(SQLException ex){
                
            }
        });
        
        updateOrderNumber.setOnAction(e->{
            try{
                String updateNumberString = "UPDATE " + tableID +  
                    " SET ordernumber = '" + tfOrderNumber.getText()
                            .toUpperCase() + 
                    "' WHERE invoice = '" + tfInvoice.getText() + "';";
                System.out.println(updateNumberString);
                PreparedStatement updateNumberStatement = connection
                        .prepareStatement(updateNumberString);
                
                updateNumberStatement.execute();
                
            }catch(SQLException ex){
                
            }
        });
        
        fieldsPane.add(new Label("Order Number : "),0,0);
        fieldsPane.add(tfOrderNumber,1,0);
        fieldsPane.add(new Label("Order Date : "),0,1);
        fieldsPane.add(tfOrderDate, 1, 1);
        fieldsPane.add(new Label("Enter as YYYY-MM-DD"),2,1);
        fieldsPane.add(new Label("Item Name : "),0,2);
        fieldsPane.add(tfItemName,1,2);
        fieldsPane.add(new Label("Item Number : "),0,3);
        fieldsPane.add(tfItemNumber,1,3);
        fieldsPane.add(new Label("Item Quantity : "),0,4);
        fieldsPane.add(tfQuantity,1,4);
        fieldsPane.add(new Label("Item Location : "),0,5);
        fieldsPane.add(tfLocation,1,5);
        fieldsPane.add(new Label("Cost : "),0,6);
        fieldsPane.add(tfItemPrice,1,6);
        fieldsPane.add(new Label("Supplier : "),0,7);
        fieldsPane.add(tfItemSupplier,1,7);
        fieldsPane.add(new Label("Invoice Number : "),0,8);
        fieldsPane.add(tfInvoice,1,8);
        
        
        buttonBox.add(clearAllButton,0,0);
        buttonBox.add(newItemButton,0,1);
        buttonBox.add(sameSupplier,1,1);
        buttonBox.add(addToDatabaseButton,0,2);
        buttonBox.add(updateDatabase,0,3);
        buttonBox.add(updateOrderNumber,0,4);
        buttonBox.setPadding(padding);
        
        userGUI.getChildren().addAll(fieldsPane,buttonBox);
        userGUI.setPadding(padding);
        
        HBox deleteBox = new HBox();
        deleteBox.setPadding(padding);
        
        if(tableID.equals("main_inventory")){
            status.setText("*** Using Maintenance Database ***");
        } else if(tableID.equals("hk_inventory")){
            status.setText("*** Using Housekeeping Database ***");
        }
        
        
        deleteBox.getChildren().addAll(deleteOrderButton,deleteItemButton);
        VBox bottom = new VBox();
        
        bottom.getChildren().addAll(deleteBox,status);
        
        fullScene.getChildren().addAll(userGUI,bottom);
        
        root.setTop(menuBar);
        root.setCenter(fullScene);
        
        return new Scene(root,300,250);
    }
    
    // GUI SCENE FOR SUPPLIER INFO
    private Scene supplierInfoScene(){
        BorderPane root = new BorderPane();
        GridPane displayInfo = new GridPane();
        displayInfo.setPadding(new Insets(10,10,10,10));
        TextArea taDisplay = new TextArea();
        ChoiceBox suppliersBox = new ChoiceBox();
        Button contactSearch = new Button("Search");
        Button addSupplier = new Button("Add new supplier");
        Button editComment = new Button("Edit comment");
        
        addSupplier.setOnAction(e->{
           addSupplierInfo(); 
        });
        
        editComment.setOnAction(e->{
           editSupplierComment(); 
        });
        
        try{
            Statement optionsForCombo = connection.createStatement();
            ResultSet rSetForOptions = optionsForCombo.executeQuery(
                    "select supplier from suppliers;");
            ArrayList<String> optionsList = new ArrayList<>();
            
            while(rSetForOptions.next()){
                String option = rSetForOptions.getObject("supplier").toString();
                optionsList.add(option);
                }
            ObservableList<String> optionsBoxList = FXCollections.
                    observableArrayList(optionsList);
            suppliersBox.setItems(optionsBoxList);
            
        }catch(SQLException ex){
            
        }
        
        contactSearch.setOnAction(e->{
            String supplierDetailsList="",supplier="",account="",contact="",
                phone="",email="",website="",comment="";
            try{
                Statement contactStatement = null;
                ResultSet rSetOfContacts = null;
                
                contactStatement = connection.createStatement();
                rSetOfContacts = contactStatement.executeQuery("select * from "
                + "suppliers where supplier = '" + suppliersBox.getValue().
                        toString() +
                        " ';");
                
                while(rSetOfContacts.next()){
                    supplier = rSetOfContacts.getObject("supplier").toString();
                    account = rSetOfContacts.
                            getObject("accountnumber").toString();
                    contact = rSetOfContacts.getObject("contact").toString();
                    phone = rSetOfContacts.getObject("phone").toString();
                    email = rSetOfContacts.getObject("email").toString();
                    website = rSetOfContacts.getObject("website").toString();
                    comment = rSetOfContacts.getObject("comments").toString();
                    
                    supplierDetailsList = "Supplier: " + supplier + "\n" +
                            "Account Number: " + account + "\n" + "Contact: " +
                            contact + "\n" + "Phone: " + phone + "\n" + 
                            "Email: " + email + "\n" + "Website: " + website +
                            "\n" + "Comments: " + comment;
                    
                    taDisplay.setText(supplierDetailsList);
                }       
            }catch(SQLException ex){
            }
        });
        
        displayInfo.add(new Label("Supplier Name : "),0,0);
        displayInfo.add(suppliersBox,1,0);
        displayInfo.add(taDisplay,2,0);
        displayInfo.add(contactSearch,0,1);
        displayInfo.add(addSupplier,1,1);
        displayInfo.add(editComment,2,1);
        
        root.setTop(menuBar);
        root.setCenter(displayInfo);
        
        return new Scene(root,300,250);
    }
    
    // ADD SUPPLIER TO DATABASE METHOD
    private void addSupplierInfo(){
        Alert supplierInfo = new Alert(AlertType.CONFIRMATION);
        supplierInfo.setHeaderText("Enter Supplier Information.");
        
        GridPane supplierGrid = new GridPane();
        supplierGrid.setHgap(10);
        supplierGrid.setVgap(10);
        supplierGrid.setPadding(new Insets(20,150,10,10));
        
        TextField supplierName = new TextField();
        supplierName.setPromptText("Supplier name");
        TextField accountNumber = new TextField();
        accountNumber.setPromptText("Enter 'NONE' if not applicable");
        TextField contactInfo = new TextField();
        contactInfo.setPromptText("Enter 'NONE' if not applicable");
        TextField phone = new TextField();
        phone.setPromptText("Phone Number");
        TextField email = new TextField();
        email.setPromptText("Email");
        TextField website = new TextField();
        website.setPromptText("Enter 'NONE' if not applicable");
        
        supplierGrid.add(new Label("Supplier Name: "),0,0);
        supplierGrid.add(supplierName,1,0);
        supplierGrid.add(new Label("Account Number:"),0,1);
        supplierGrid.add(accountNumber,1,1);
        supplierGrid.add(new Label("Contact:"),0,2);
        supplierGrid.add(contactInfo,1,2);
        supplierGrid.add(new Label("Phone Number:"),0,3);
        supplierGrid.add(phone,1,3);
        supplierGrid.add(new Label("Email:"),0,4);
        supplierGrid.add(email,1,4);
        supplierGrid.add(new Label("Website:"),0,5);
        supplierGrid.add(website,1,5);
        
        supplierInfo.getDialogPane().setContent(supplierGrid);
        
        Optional<ButtonType> result = supplierInfo.showAndWait();
        
        if(result.isPresent()){
            String addStatement = "insert into suppliers (supplier, "
                    + "accountnumber, contact, phone, email, website) values"
                    + "('" + supplierName.getText().toUpperCase() +"', '"+ 
                    accountNumber.getText().toUpperCase() + "', '" + 
                    contactInfo.getText().toUpperCase() + "', '" + 
                    phone.getText().toUpperCase() + "', '" + 
                    email.getText().toUpperCase() + "', '" + 
                    website.getText().toUpperCase() + "');";
            try{
                PreparedStatement addSuppState = connection
                        .prepareStatement(addStatement);
                addSuppState.execute();
                
            }catch(SQLException ex){
                ex.printStackTrace();
            }
        }
        
    }
    
    // EDIT SUPPLIER CONTACT DATABASE METHOD
    private void editSupplierComment(){
        Dialog editCommentDialog = new Dialog();
        editCommentDialog.setHeaderText("Edit comment for supplier");
        
        GridPane editGrid = new GridPane();
        editGrid.setHgap(10);
        editGrid.setVgap(10);
        editGrid.setPadding(new Insets(20,150,10,10));
        
        TextArea commentArea = new TextArea();
        Label supplierLabel = new Label();
        ChoiceBox suppliersNameBox = new ChoiceBox();
        Button searchSupplierButton = new Button();
        Button updateComment = new Button();
        
        try{
            Statement optionsForCombo = connection.createStatement();
            ResultSet rSetForOptions = optionsForCombo.executeQuery(
                    "select supplier from suppliers;");
            ArrayList<String> optionsList = new ArrayList<>();
            
            while(rSetForOptions.next()){
                String option = rSetForOptions.getObject("supplier").toString();
                optionsList.add(option);
                }
            ObservableList<String> optionsBoxList = FXCollections.
                    observableArrayList(optionsList);
            suppliersNameBox.setItems(optionsBoxList);
            
        }catch(SQLException ex){
            
        }
        
        searchSupplierButton.setOnAction(e->{
            try {
                Statement searchSupplier = connection.createStatement();
                ResultSet rSetForSuppliers = searchSupplier.executeQuery(
                    "select * from suppliers where supplier = ' + "
                    + suppliersNameBox.getValue().toString() + "';" );
                        
                while (rSetForSuppliers.next()){
                    commentArea.setText(rSetForSuppliers.getObject("comments")
                    .toString().trim());
                    supplierLabel.setText(rSetForSuppliers.getObject("supplier")
                    .toString().trim());
                }
            } catch (SQLException ex) {
                
            }
        });
        
        updateComment.setOnAction(e->{
           String commentUpdateString = "update suppliers set comments = '" +
                commentArea.getText().toString().toUpperCase() + 
                   "' where supplier = '" +
                   suppliersNameBox.getValue().toString() + "';";
            try {
               PreparedStatement commentUpdateSQL = connection
                       .prepareStatement(commentUpdateString);
               commentUpdateSQL.execute();
           } catch (SQLException ex){
               
           }
        });
        
        HBox buttonBox = new HBox();
        buttonBox.setPadding(new Insets(5,5,5,5));
        
        editGrid.add(commentArea,0,0);
        editGrid.add(supplierLabel,0,1);
        editGrid.add(suppliersNameBox,1,1);
        editGrid.add(buttonBox,0,2);
        
        editCommentDialog.getDialogPane().setContent(editGrid);
        editCommentDialog.showAndWait();
    }
    
    // METHOD TO CLEAR GUI TEXTFIELDS
    private void clearFields(){
        tfOrderNumber.setText("");
        tfOrderDate.setText("");
        tfItemName.setText("");
        tfItemNumber.setText("");
        tfQuantity.setText("");
        tfLocation.setText("");
        tfItemPrice.setText("");
        tfItemSupplier.setText("");
        tfInvoice.setText("");
    }
    
    // METHOD TO PRINT TEXTAREA FOR COMPLETE ORDERS
    private void print(String string) throws UnsupportedEncodingException,
            PrintException, IOException{
        String orderDetailsLine = "", orderDetailsList = "", item = "", 
                itemNumber = "", location = "", quantity = "", price = "";
        Statement statementToSearch = null;
        ResultSet rSetSearch = null;
        
        String labelDescriptor = String.format(
        "%1s%3s%-50s%3s%-15s%3s%-7s%3s%-8s", "X"," | ", "Item"," | ",
        "ItemNumber"," | ","Price"," | ","Quantity");
        
        orderDetailsList += ("Order Number: " + tfOrderNumber.getText() +"\n");
        orderDetailsList += ("Order Date: " + tfOrderDate.getText() + "\n");
        orderDetailsList += ("Invoice Number: " + tfInvoice.getText() + "\n");
        orderDetailsList += ("Supplier: " + tfItemSupplier.getText() + "\n");
        orderDetailsList += ("\n" + "\n" + labelDescriptor + "\n" + "\n");
        
        try{
            statementToSearch = connection.createStatement();
            rSetSearch = statementToSearch.executeQuery("select * from " +
                    tableID + " where ordernumber ='" + 
                    tfOrderNumber.getText() +"';");
            
            while(rSetSearch.next()){
                location = rSetSearch.getObject("location").toString().trim();
                item = rSetSearch.getObject("item").toString().trim();
                itemNumber = rSetSearch.getObject("itemnumber").toString()
                        .trim();
                price = rSetSearch.getObject("price").toString().trim();
                quantity = rSetSearch.getObject("quantity").toString().trim();
                
                orderDetailsLine = String.format(
                        "%1s%3s%-50s%3s%-15s%3s%-7s%3s%-8s",location," | ",item,
                        " | ",itemNumber," | ",price," | ",quantity," | ");
                
                orderDetailsList += (orderDetailsLine + "\n");
            }
        }catch(SQLException ex){
            
        }
        String defaultPrinter = 
                PrintServiceLookup.lookupDefaultPrintService().getName();
            
        System.out.println("Default printer: " + defaultPrinter);
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();

        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        pras.add(OrientationRequested.LANDSCAPE);
        pras.add(new Copies(1));

        DocFlavor flavor = DocFlavor.STRING.TEXT_PLAIN;
        Doc doc = new SimpleDoc(orderDetailsList,flavor,null);
        DocPrintJob job = service.createPrintJob();

        PrintJobWatcher pjw = new PrintJobWatcher(job);
        job.print(doc, pras);
        pjw.waitForDone();
        
    }

    public static void main(String[] args) {
        launch(args);
    }

}

