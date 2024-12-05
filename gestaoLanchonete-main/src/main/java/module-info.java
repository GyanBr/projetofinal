module br.com.professorclaytonandrade.sistemaservicosjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires commons.dbcp2;
    requires com.zaxxer.hikari;
    requires org.slf4j;

    opens br.com.professorclaytonandrade.sistemaservicosjavafx to javafx.fxml;
    exports br.com.professorclaytonandrade.sistemaservicosjavafx;
    //exports br.com.professorclaytonandrade.sistemaservicosjavafx.dto;
    opens br.com.professorclaytonandrade.sistemaservicosjavafx.controller to javafx.fxml;
   // opens br.com.professorclaytonandrade.sistemaservicosjavafx.dto to javafx.base;

}
