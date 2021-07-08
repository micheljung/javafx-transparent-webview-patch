# javafx-transparent-webview-patch
Allows transparent background for JavaFX's WebView. Credit goes to https://gist.github.com/riccardobl/18603f9de508b1ab6c9e

Build the patcher jar-file first using `.\gradlew jar`, then use the provided run configurations.

## Notes

JavaFX doesn't support WebViews with transparent backgrounds yet. See [JDK-8090547](https://bugs.openjdk.java.net/browse/JDK-8090547).
This workaround enables transparent WebView backgrounds but comes with a big performance penalty since the whole WebView is repainted,
rather than just dirty areas.

On 2021-07-03, the CSR [JDK-8269848](https://bugs.openjdk.java.net/browse/JDK-8269848) was opened to officially fix this 
