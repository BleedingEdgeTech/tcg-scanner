# ProGuard rules for the MTG OCR Android app

# Keep the application class
-keep class com.example.mtgocr.MtgOcrApp { *; }

# Keep all activities
-keep class com.example.mtgocr.**Activity { *; }

# Keep all ViewModels
-keep class com.example.mtgocr.ui.**ViewModel { *; }

# Keep all data classes
-keep class com.example.mtgocr.domain.model.** { *; }

# Keep all entities
-keep class com.example.mtgocr.data.local.entity.** { *; }

# Keep all repositories
-keep class com.example.mtgocr.repository.** { *; }

# Keep all use cases
-keep class com.example.mtgocr.domain.usecase.** { *; }

# Keep all API interfaces
-keep interface com.example.mtgocr.data.remote.** { *; }

# Keep all components
-keep class com.example.mtgocr.ui.components.** { *; }

# Keep all utils
-keep class com.example.mtgocr.util.** { *; }

# Keep all classes that are referenced in XML
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.fragment.app.Fragment { *; }
-keep class * extends androidx.appcompat.app.AppCompatActivity { *; }

# Keep Gson serialization
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep the CSV exporter
-keep class com.example.mtgocr.data.export.CsvExporter { *; }

# Keep the camera classes
-keep class com.example.mtgocr.ui.camera.** { *; }

# Keep the history classes
-keep class com.example.mtgocr.ui.history.** { *; }

# Keep the theme classes
-keep class com.example.mtgocr.ui.theme.** { *; }

# Keep the navigation graph
-keep class com.example.mtgocr.ui.navigation.NavGraph { *; }