package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;
import java.util.Optional;

public class Main extends Application {
    private ObservableList<Animal> animals = FXCollections.observableArrayList();
    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    private boolean isDarkMode = false;

    @Override
    public void start(Stage primaryStage) {
        loadCustomersFromFile("customers.txt");
        loadAnimalsFromFile("animals.txt");
        primaryStage.setTitle("Animal Management App");
        primaryStage.setFullScreen(true);
        Scene loginScene = createLoginScene(primaryStage);
        applyTheme(loginScene);
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private Scene createLoginScene(Stage primaryStage) {
        VBox layout = createBasicLayout();
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Label messageLabel = new Label();

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        Button themeToggleButton = createThemeToggleButton(primaryStage);

        loginButton.setOnAction(event -> {
            String email = emailField.getText();
            String password = passwordField.getText();

            Optional<Customer> customerOpt = customers.stream()
                    .filter(c -> c.getEmail().equals(email) && c.getPassword().equals(password))
                    .findFirst();

            if (customerOpt.isPresent()) {
                Scene customerScene = createCustomerScene(primaryStage, customerOpt.get());
                applyTheme(customerScene);
                primaryStage.setScene(customerScene);
            } else if (email.equals("admin@example.com") && password.equals("admin123")) {
                Scene adminScene = createAdminScene(primaryStage);
                applyTheme(adminScene);
                primaryStage.setScene(adminScene);
            } else {
                messageLabel.setText("Invalid credentials. Try again.");
            }
        });

        registerButton.setOnAction(event -> {
            Scene registerScene = createRegisterScene(primaryStage);
            applyTheme(registerScene);
            primaryStage.setScene(registerScene);
        });

        layout.getChildren().addAll(emailLabel, emailField, passwordLabel, passwordField, loginButton, registerButton, themeToggleButton, messageLabel);

        return new Scene(layout, getScreenWidth(), getScreenHeight());
    }

    private Scene createCustomerScene(Stage primaryStage, Customer customer) {
        VBox layout = createBasicLayout();

        Label header = new Label("Welcome, " + customer.getName());
        header.getStyleClass().add("header");

        for (String category : new String[]{"Mammals", "Birds", "Fish", "Amphibians"}) {
            Button button = new Button(category);
            button.setOnAction(event -> {
                Scene animalListScene = createAnimalListScene(primaryStage, category, customer);
                applyTheme(animalListScene);
                primaryStage.setScene(animalListScene);
            });
            layout.getChildren().add(button);
        }

        Button depositButton = new Button("Deposit Money");
        depositButton.setOnAction(event -> {
            Scene depositScene = createDepositScene(primaryStage, customer);
            applyTheme(depositScene);
            primaryStage.setScene(depositScene);
        });

        Button backButton = new Button("Logout");
        backButton.setOnAction(event -> {
            Scene loginScene = createLoginScene(primaryStage);
            applyTheme(loginScene);
            primaryStage.setScene(loginScene);
        });

        layout.getChildren().addAll(header, depositButton, backButton);
        return new Scene(layout, getScreenWidth(), getScreenHeight());
    }

    private void sortByAge(ObservableList<Animal> animals) {
        for (int i = 0; i < animals.size() - 1; i++) {
            for (int j = 0; j < animals.size() - i - 1; j++) {
                if (animals.get(j).getAge() > animals.get(j + 1).getAge()) {
                    Animal temp = animals.get(j);
                    animals.set(j, animals.get(j + 1));
                    animals.set(j + 1, temp);
                }
            }
        }
    }

    private void sortByWeight(ObservableList<Animal> animals) {
        for (int i = 0; i < animals.size() - 1; i++) {
            for (int j = 0; j < animals.size() - i - 1; j++) {
                if (animals.get(j).getWeight() > animals.get(j + 1).getWeight()) {
                    Animal temp = animals.get(j);
                    animals.set(j, animals.get(j + 1));
                    animals.set(j + 1, temp);
                }
            }
        }
    }

    
    private Scene createManageAnimalsScene(Stage primaryStage) {
        VBox layout = createBasicLayout();

        Label header = new Label("Manage Animals");
        ListView<Animal> animalListView = new ListView<>(animals);

        TextField nameField = new TextField();
        nameField.setPromptText("Animal Name");

        TextField ageField = new TextField();
        ageField.setPromptText("Animal Age");

        TextField weightField = new TextField();
        weightField.setPromptText("Animal Weight");

        TextField imgUrlField = new TextField();
        imgUrlField.setPromptText("Image URL");

        ComboBox<String> typeDropdown = new ComboBox<>();
        typeDropdown.getItems().addAll("Lion", "Parrot", "Goldfish", "Frog", "Cat");
        typeDropdown.setPromptText("Select Type");

        Button addButton = new Button("Add Animal");
        addButton.setOnAction(event -> {
            String name = nameField.getText();
            String ageText = ageField.getText();
            String weightText = weightField.getText();
            String imgUrl = imgUrlField.getText();
            String type = typeDropdown.getValue();

            if (name.isEmpty() || ageText.isEmpty() || weightText.isEmpty() || imgUrl.isEmpty() || type == null) {
                showAlert("Validation Error", "All fields are required.");
                return;
            }

            try {
                int age = Integer.parseInt(ageText);
                double weight = Double.parseDouble(weightText);

                Animal newAnimal = switch (type.toLowerCase()) {
                    case "lion" -> new Lion(name, age, weight, imgUrl);
                    case "parrot" -> new Parrot(name, age, weight, imgUrl);
                    case "goldfish" -> new Goldfish(name, age, weight, imgUrl);
                    case "frog" -> new Frog(name, age, weight, imgUrl);
                    case "cat" -> new Cat(name, age, weight, imgUrl);
                    default -> null;
                };

                if (newAnimal != null) {
                    animals.add(newAnimal);
                    animalListView.refresh();
                    saveAnimalsToFile("animals.txt");
                    clearFields(nameField, ageField, weightField, imgUrlField, typeDropdown);
                } else {
                    showAlert("Error", "Invalid animal type selected.");
                }
            } catch (NumberFormatException e) {
                showAlert("Validation Error", "Age and Weight must be valid numbers.");
            }
        });

        Button removeButton = new Button("Remove Selected Animal");
        removeButton.setOnAction(event -> {
            Animal selectedAnimal = animalListView.getSelectionModel().getSelectedItem();
            if (selectedAnimal != null) {
                animals.remove(selectedAnimal);
                animalListView.refresh();
                saveAnimalsToFile("animals.txt");
            } else {
                showAlert("Error", "No animal selected to remove.");
            }
        });

        Button sortByAgeButton = new Button("Sort by Age");
        sortByAgeButton.setOnAction(event -> {
            sortByAge(animals);
            animalListView.refresh();
        });

        Button sortByWeightButton = new Button("Sort by Weight");
        sortByWeightButton.setOnAction(event -> {
            sortByWeight(animals);
            animalListView.refresh();
        });

        Button backButton = new Button("Back to Dashboard");
        backButton.setOnAction(event -> {
            Scene adminScene = createAdminScene(primaryStage);
            applyTheme(adminScene);
            primaryStage.setScene(adminScene);
        });

        layout.getChildren().addAll(
                header,
                animalListView,
                new Label("Add New Animal:"),
                nameField,
                ageField,
                weightField,
                imgUrlField,
                typeDropdown,
                addButton,
                removeButton,
                sortByAgeButton,
                sortByWeightButton,
                backButton
        );

        return new Scene(layout, getScreenWidth(), getScreenHeight());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields(TextField nameField, TextField ageField, TextField weightField, TextField imgUrlField, ComboBox<String> typeDropdown) {
        nameField.clear();
        ageField.clear();
        weightField.clear();
        imgUrlField.clear();
        typeDropdown.getSelectionModel().clearSelection();
    }

    private String getCategoryForAnimal(Animal animal) {
        if (animal instanceof Mammal) return "Mammals";
        if (animal instanceof Bird) return "Birds";
        if (animal instanceof Fish) return "Fish";
        if (animal instanceof Amphibian) return "Amphibians";
        return "Unknown";
    }

    private boolean animalBelongsToCategory(Animal animal, String category) {
        return switch (category) {
            case "Mammals" -> animal instanceof Mammal;
            case "Birds" -> animal instanceof Bird;
            case "Fish" -> animal instanceof Fish;
            case "Amphibians" -> animal instanceof Amphibian;
            default -> false;
        };
    }

    private Scene createAdminScene(Stage primaryStage) {
        VBox layout = createBasicLayout();

        Label header = new Label("Admin Dashboard");

        Button manageAnimalsButton = new Button("Manage Animals");
        manageAnimalsButton.setOnAction(event -> {
            Scene manageAnimalsScene = createManageAnimalsScene(primaryStage);
            applyTheme(manageAnimalsScene);
            primaryStage.setScene(manageAnimalsScene);
        });

        Button manageCustomersButton = new Button("Manage Customers");
        manageCustomersButton.setOnAction(event -> {
            Scene manageCustomersScene = createManageCustomersScene(primaryStage);
            applyTheme(manageCustomersScene);
            primaryStage.setScene(manageCustomersScene);
        });

        Button backButton = new Button("Back to Login");
        backButton.setOnAction(event -> {
            Scene loginScene = createLoginScene(primaryStage);
            applyTheme(loginScene);
            primaryStage.setScene(loginScene);
        });

        layout.getChildren().addAll(header, manageAnimalsButton, manageCustomersButton, backButton);
        return new Scene(layout, getScreenWidth(), getScreenHeight());
    }

    private Scene createAnimalDetailScene(Stage primaryStage, Animal animal, Customer customer) {
        VBox layout = createBasicLayout();

        Label header = new Label("Animal Details");
        header.getStyleClass().add("header");

        Label animalInfo = new Label(animal.toString());

        ImageView imageView = new ImageView(new Image(animal.getImgUrl()));
        imageView.setFitHeight(300);
        imageView.setFitWidth(300);

        Button buyButton = new Button("Buy Animal ($" + animal.getWeight() * 10 + ")");
        buyButton.setOnAction(event -> {
            double price = animal.getWeight() * 10;
            customer.setMoney(customer.getMoney() + price); // Add the animal's price to the customer's balance
            animals.remove(animal); // Remove the purchased animal from the list
            saveAnimalsToFile("animals.txt"); // Save the updated animal list to the file
            saveCustomersToFile("customers.txt"); // Save the updated customer balance to the file
            showAlert("Success", "You sold " + animal.getName() + " for $" + price + "!");
            Scene customerScene = createCustomerScene(primaryStage, customer);
            applyTheme(customerScene);
            primaryStage.setScene(customerScene);
        });

        Button backButton = new Button("Back to Animal List");
        backButton.setOnAction(event -> {
            Scene animalListScene = createAnimalListScene(primaryStage, getCategoryForAnimal(animal), customer);
            applyTheme(animalListScene);
            primaryStage.setScene(animalListScene);
        });

        layout.getChildren().addAll(header, imageView, animalInfo, buyButton, backButton);
        return new Scene(layout, getScreenWidth(), getScreenHeight());
    }


    private Scene createAnimalListScene(Stage primaryStage, String category, Customer customer) {
        VBox layout = createBasicLayout();

        Label header = new Label("Category: " + category);
        header.getStyleClass().add("header");

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search for an animal...");

        ListView<Animal> animalListView = new ListView<>();
        ObservableList<Animal> filteredAnimals = FXCollections.observableArrayList();

        for (Animal animal : animals) {
            if (animalBelongsToCategory(animal, category)) {
                filteredAnimals.add(animal);
            }
        }
        animalListView.setItems(filteredAnimals);

        animalListView.setOnMouseClicked(event -> {
            Animal selectedAnimal = animalListView.getSelectionModel().getSelectedItem();
            if (selectedAnimal != null) {
                Scene detailScene = createAnimalDetailScene(primaryStage, selectedAnimal, customer);
                applyTheme(detailScene);
                primaryStage.setScene(detailScene);
            }
        });

        Button backButton = new Button("Back to Categories");
        backButton.setOnAction(event -> {
            Scene customerScene = createCustomerScene(primaryStage, customer);
            applyTheme(customerScene);
            primaryStage.setScene(customerScene);
        });

        layout.getChildren().addAll(header, searchBar, animalListView, backButton);
        return new Scene(layout, getScreenWidth(), getScreenHeight());
    }

    private Scene createRegisterScene(Stage primaryStage) {
        VBox layout = createBasicLayout();
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Label messageLabel = new Label();

        Button registerButton = new Button("Register");
        registerButton.setOnAction(event -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                messageLabel.setText("All fields are required!");
                return;
            }

            Optional<Customer> existingCustomer = customers.stream()
                    .filter(c -> c.getEmail().equals(email))
                    .findFirst();

            if (existingCustomer.isPresent()) {
                messageLabel.setText("A user with this email already exists.");
            } else {
                customers.add(new Customer(name, email, password));
                saveCustomersToFile("customers.txt");
                Scene loginScene = createLoginScene(primaryStage);
                applyTheme(loginScene);
                primaryStage.setScene(loginScene);
            }
        });

        Button backButton = new Button("Back to Login");
        backButton.setOnAction(event -> {
            Scene loginScene = createLoginScene(primaryStage);
            applyTheme(loginScene);
            primaryStage.setScene(loginScene);
        });

        layout.getChildren().addAll(new Label("Register"), nameField, emailField, passwordField, registerButton, backButton, messageLabel);

        return new Scene(layout, getScreenWidth(), getScreenHeight());
    }

    private Scene createDepositScene(Stage primaryStage, Customer customer) {
        VBox layout = createBasicLayout();

        Label header = new Label("Deposit Money");

        Label balanceLabel = new Label("Current Balance: $" + customer.getMoney());

        TextField depositField = new TextField();
        depositField.setPromptText("Deposit Amount");

        Button depositButton = new Button("Deposit");
        depositButton.setOnAction(event -> {
            try {
                double amount = Double.parseDouble(depositField.getText());
                if (amount > 0) {
                    customer.addMoney(amount);
                    balanceLabel.setText("Current Balance: $" + customer.getMoney());
                    saveCustomersToFile("customers.txt");
                } else {
                    balanceLabel.setText("Invalid amount entered.");
                }
            } catch (NumberFormatException e) {
                balanceLabel.setText("Please enter a valid number.");
            }
        });

        Button backButton = new Button("Back to Categories");
        backButton.setOnAction(event -> {
            Scene customerScene = createCustomerScene(primaryStage, customer);
            applyTheme(customerScene);
            primaryStage.setScene(customerScene);
        });

        layout.getChildren().addAll(header, balanceLabel, depositField, depositButton, backButton);
        return new Scene(layout, getScreenWidth(), getScreenHeight());
    }

    private Scene createManageCustomersScene(Stage primaryStage) {
        VBox layout = createBasicLayout();

        Label header = new Label("Manage Customers");
        ListView<Customer> customerListView = new ListView<>(customers);

        Button removeButton = new Button("Remove Selected Customer");
        removeButton.setOnAction(event -> {
            Customer selected = customerListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                customers.remove(selected);
                saveCustomersToFile("customers.txt");
                customerListView.refresh();
            }
        });

        Button backButton = new Button("Back to Dashboard");
        backButton.setOnAction(event -> {
            Scene adminScene = createAdminScene(primaryStage);
            applyTheme(adminScene);
            primaryStage.setScene(adminScene);
        });

        layout.getChildren().addAll(header, customerListView, removeButton, backButton);
        return new Scene(layout, getScreenWidth(), getScreenHeight());
    }

    private VBox createBasicLayout() {
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        return layout;
    }

    private double getScreenWidth() {
        return Screen.getPrimary().getBounds().getWidth();
    }

    private double getScreenHeight() {
        return Screen.getPrimary().getBounds().getHeight();
    }

    private Button createThemeToggleButton(Stage primaryStage) {
        Button themeToggleButton = new Button("Toggle Theme");
        themeToggleButton.setOnAction(event -> {
            isDarkMode = !isDarkMode;
            applyTheme(primaryStage.getScene());
        });
        return themeToggleButton;
    }

    private void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        String theme = isDarkMode ? "dark.css" : "application.css";
        scene.getStylesheets().add(getClass().getResource(theme).toExternalForm());
    }

    private void loadCustomersFromFile(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 4) {
                        customers.add(new Customer(parts[0], parts[1], parts[2], Double.parseDouble(parts[3])));
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    private void loadAnimalsFromFile(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 5) {
                        Animal animal = switch (parts[0].toLowerCase()) {
                            case "lion" -> new Lion(parts[1], Integer.parseInt(parts[2]), Double.parseDouble(parts[3]), parts[4]);
                            case "parrot" -> new Parrot(parts[1], Integer.parseInt(parts[2]), Double.parseDouble(parts[3]), parts[4]);
                            case "goldfish" -> new Goldfish(parts[1], Integer.parseInt(parts[2]), Double.parseDouble(parts[3]), parts[4]);
                            case "frog" -> new Frog(parts[1], Integer.parseInt(parts[2]), Double.parseDouble(parts[3]), parts[4]);
                            case "cat" -> new Cat(parts[1], Integer.parseInt(parts[2]), Double.parseDouble(parts[3]), parts[4]);
                            default -> null;
                        };
                        if (animal != null) animals.add(animal);
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    private void saveCustomersToFile(String filename) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for (Customer customer : customers) {
                bw.write(String.format("%s,%s,%s,%.2f", customer.getName(), customer.getEmail(), customer.getPassword(), customer.getMoney()));
                bw.newLine();
            }
        } catch (IOException ignored) {
        }
    }

    private void saveAnimalsToFile(String filename) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for (Animal animal : animals) {
                bw.write(String.format("%s,%s,%d,%.2f,%s", animal.getClass().getSimpleName(), animal.getName(), animal.getAge(), animal.getWeight(), animal.getImgUrl()));
                bw.newLine();
            }
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

abstract class User {
    private String name;
    private String email;
    private String password;

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

class Customer extends User {
    private double money;

    public Customer(String name, String email, String password) {
        super(name, email, password);
        this.money = 0.0;
    }

    public Customer(String name, String email, String password, double money) {
        super(name, email, password);
        this.money = money;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public void addMoney(double amount) {
        this.money += amount;
    }

    @Override
    public String toString() {
        return String.format("Name: %s, Email: %s, Balance: $%.2f", getName(), getEmail(), getMoney());
    }
}

abstract class Animal {
    private String name;
    private int age;
    private double weight;
    private String imgUrl;

    public Animal(String name, int age, double weight, String imgUrl) {
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.imgUrl = imgUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    @Override
    public String toString() {
        return String.format("%s (Age: %d, Weight: %.2f)", name, age, weight);
    }
}

class Mammal extends Animal {
    public Mammal(String name, int age, double weight, String imgUrl) {
        super(name, age, weight, imgUrl);
    }
}

class Lion extends Mammal {
    public Lion(String name, int age, double weight, String imgUrl) {
        super(name, age, weight, imgUrl);
    }
}

class Cat extends Mammal {
    public Cat(String name, int age, double weight, String imgUrl) {
        super(name, age, weight, imgUrl);
    }
}

class Bird extends Animal {
    public Bird(String name, int age, double weight, String imgUrl) {
        super(name, age, weight, imgUrl);
    }
}

class Parrot extends Bird {
    public Parrot(String name, int age, double weight, String imgUrl) {
        super(name, age, weight, imgUrl);
    }
}

class Fish extends Animal {
    public Fish(String name, int age, double weight, String imgUrl) {
        super(name, age, weight, imgUrl);
    }
}

class Goldfish extends Fish {
    public Goldfish(String name, int age, double weight, String imgUrl) {
        super(name, age, weight, imgUrl);
    }
}

class Amphibian extends Animal {
    public Amphibian(String name, int age, double weight, String imgUrl) {
        super(name, age, weight, imgUrl);
    }
}

class Frog extends Amphibian {
    public Frog(String name, int age, double weight, String imgUrl) {
        super(name, age, weight, imgUrl);
    }
}