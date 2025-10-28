using UnityEngine;

/// <summary>
/// Unity side bridge for communication with Kotlin/Android app.
/// Attach this script to a GameObject in your Unity scene.
/// Name the GameObject "AndroidBridge" for easy access from Kotlin.
/// </summary>
public class AndroidBridge : MonoBehaviour
{
    private AndroidJavaObject currentActivity;
    
    void Start()
    {
        // Get reference to the current Android activity
        try
        {
            AndroidJavaClass unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
            currentActivity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity");
            Debug.Log("AndroidBridge: Connected to Android activity");
        }
        catch (System.Exception e)
        {
            Debug.LogError($"AndroidBridge: Failed to connect to Android activity: {e.Message}");
        }
    }
    
    /// <summary>
    /// Called from Kotlin to pass data to Unity
    /// Usage in Kotlin: UnityBridge.sendToUnity("AndroidBridge", "ReceiveMessage", "Hello Unity")
    /// </summary>
    public void ReceiveMessage(string message)
    {
        Debug.Log($"AndroidBridge: Received message from Kotlin: {message}");
        // Handle the message here
        // For example, you could parse JSON data or trigger game events
    }
    
    /// <summary>
    /// Called from Kotlin to load a specific level/scene
    /// Usage in Kotlin: UnityBridge.sendToUnity("AndroidBridge", "LoadLevel", "SolarSystem")
    /// </summary>
    public void LoadLevel(string levelName)
    {
        Debug.Log($"AndroidBridge: Loading level: {levelName}");
        // Load the specified level
        // UnityEngine.SceneManagement.SceneManager.LoadScene(levelName);
    }
    
    /// <summary>
    /// Called from Kotlin to pass user data (e.g., score, progress)
    /// Usage in Kotlin: UnityBridge.sendToUnity("AndroidBridge", "SetUserData", "{\"score\":85,\"level\":3}")
    /// </summary>
    public void SetUserData(string jsonData)
    {
        Debug.Log($"AndroidBridge: Received user data: {jsonData}");
        // Parse JSON and use the data
        // Example: JsonUtility.FromJson<UserData>(jsonData);
    }
    
    /// <summary>
    /// Call this method to exit Unity and return to the Kotlin app
    /// Usage in Unity: GetComponent<AndroidBridge>().ExitToKotlinApp();
    /// </summary>
    public void ExitToKotlinApp()
    {
        Debug.Log("AndroidBridge: Exiting Unity and returning to Kotlin app");
        
        try
        {
            // Call the exitUnity method in the UnityHolderActivity
            if (currentActivity != null)
            {
                currentActivity.Call("exitUnity");
            }
        }
        catch (System.Exception e)
        {
            Debug.LogError($"AndroidBridge: Failed to exit Unity: {e.Message}");
            // Fallback: Quit the application
            Application.Quit();
        }
    }
    
    /// <summary>
    /// Send data back to Kotlin app (optional - for advanced scenarios)
    /// This would require setting up a listener in the Kotlin activity
    /// </summary>
    public void SendToKotlin(string methodName, string data)
    {
        try
        {
            if (currentActivity != null)
            {
                currentActivity.Call(methodName, data);
                Debug.Log($"AndroidBridge: Sent to Kotlin - {methodName}({data})");
            }
        }
        catch (System.Exception e)
        {
            Debug.LogError($"AndroidBridge: Failed to send to Kotlin: {e.Message}");
        }
    }
    
    /// <summary>
    /// Example: Send quiz result back to Kotlin
    /// </summary>
    public void SendQuizResult(int score, bool completed)
    {
        string result = $"{{\"score\":{score},\"completed\":{completed.ToString().ToLower()}}}";
        SendToKotlin("onQuizCompleted", result);
    }
}

// Example usage in your Unity game scripts:
// 
// To exit Unity and return to Kotlin app:
// FindObjectOfType<AndroidBridge>().ExitToKotlinApp();
//
// To send quiz results:
// FindObjectOfType<AndroidBridge>().SendQuizResult(85, true);
