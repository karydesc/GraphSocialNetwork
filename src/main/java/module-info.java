module com.assign1.assignment {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.assign1.assignment to javafx.fxml;
    exports com.assign1.assignment;
}