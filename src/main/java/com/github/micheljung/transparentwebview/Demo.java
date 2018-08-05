package com.github.micheljung.transparentwebview;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Demo extends Application {

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(final Stage stage) {
    WebView webView = new WebView();

    final WebEngine webEngine = webView.getEngine();
    webEngine.loadContent("<html><head></head><body>If this is black text on red background, the patcher was successful</body></html>");

    VBox root = new VBox();
    root.setBackground(new Background(new BackgroundFill(Paint.valueOf("red"), null, null)));
    root.getChildren().add(webView);

    Scene scene = new Scene(root);
    stage.setScene(scene);
    stage.show();
  }
}
