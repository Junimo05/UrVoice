
@file:Suppress(
  "KotlinRedundantDiagnosticSuppress",
  "LocalVariableName",
  "MayBeConstant",
  "RedundantVisibilityModifier",
  "RemoveEmptyClassBody",
  "SpellCheckingInspection",
  "LocalVariableName",
  "unused",
)

package connectors.default

import com.google.firebase.FirebaseApp
import com.google.firebase.dataconnect.ConnectorConfig
import com.google.firebase.dataconnect.DataConnectSettings
import com.google.firebase.dataconnect.FirebaseDataConnect
import com.google.firebase.dataconnect.generated.GeneratedConnector
import com.google.firebase.dataconnect.getInstance
import java.util.WeakHashMap

public interface DefaultConnector : GeneratedConnector {
  override val dataConnect: FirebaseDataConnect

  

  public companion object {
    @Suppress("MemberVisibilityCanBePrivate")
    public val config: ConnectorConfig = ConnectorConfig(
      connector = "default",
      location = "us-central1",
      serviceId = "TheVoices",
    )

    public fun getInstance(
      dataConnect: FirebaseDataConnect
    ):DefaultConnector = synchronized(instances) {
      instances.getOrPut(dataConnect) {
        DefaultConnectorImpl(dataConnect)
      }
    }

    private val instances = WeakHashMap<FirebaseDataConnect, DefaultConnectorImpl>()
  }
}

public val DefaultConnector.Companion.instance:DefaultConnector
  get() = getInstance(FirebaseDataConnect.getInstance(config))

public fun DefaultConnector.Companion.getInstance(
  settings: DataConnectSettings = DataConnectSettings()
):DefaultConnector =
  getInstance(FirebaseDataConnect.getInstance(config, settings))

public fun DefaultConnector.Companion.getInstance(
  app: FirebaseApp,
  settings: DataConnectSettings = DataConnectSettings()
):DefaultConnector =
  getInstance(FirebaseDataConnect.getInstance(app, config, settings))

private class DefaultConnectorImpl(
  override val dataConnect: FirebaseDataConnect
) : DefaultConnector {
  

  override fun equals(other: Any?): Boolean = other === this

  override fun hashCode(): Int = System.identityHashCode(this)

  override fun toString() = "DefaultConnectorImpl(dataConnect=$dataConnect)"
}



// The lines below are used by the code generator to ensure that this file is deleted if it is no
// longer needed. Any files in this directory that contain the lines below will be deleted by the
// code generator if the file is no longer needed. If, for some reason, you do _not_ want the code
// generator to delete this file, then remove the line below (and this comment too, if you want).

// FIREBASE_DATA_CONNECT_GENERATED_FILE MARKER 42da5e14-69b3-401b-a9f1-e407bee89a78
// FIREBASE_DATA_CONNECT_GENERATED_FILE CONNECTOR default
