# Android App Update Guide - Teacher Selection Dropdown

## Current Status: Database Migration Complete ✅

Now we need to update the Android app to use the new `teacher_id` system.

---

## 📋 Implementation Checklist

### Part 1: Update UserRepository.kt ⏳

**File**: `app/src/main/java/com/example/escape_ar/data/repository/UserRepository.kt`

#### Step 1.1: Add Teacher Data Class

Find the top of the file (after imports, around line 26) and add:

```kotlin
// Add this after the KylonModules object definition
data class Teacher(
    val id: String,
    val displayName: String,
    val email: String
)
```

#### Step 1.2: Add getAvailableTeachers() Function

Add this function to the `UserRepository` class (around line 100, before `signUp` function):

```kotlin
suspend fun getAvailableTeachers(): Result<List<Teacher>> = withContext(Dispatchers.IO) {
    try {
        val rawBase = SupabaseConfig.client.supabaseUrl
        val base = normalizeBaseUrl(rawBase)
        
        if (base.isBlank() || base.contains("localhost", ignoreCase = true)) {
            return@withContext Result.failure(IllegalStateException("Invalid Supabase URL"))
        }
        
        val url = "$base/rest/v1/profiles?role=eq.teacher&select=id,display_name,email&order=display_name.asc"
        
        val response = http.get(url) {
            header("apikey", SupabaseConfig.client.supabaseKey)
            header("Accept", "application/json")
        }
        
        if (!response.status.isSuccess()) {
            val body = response.bodyAsText()
            Log.e("UserRepository", "getAvailableTeachers failed: ${response.status} - $body")
            return@withContext Result.failure(
                IllegalStateException("Failed to fetch teachers: ${response.status}")
            )
        }
        
        val body = response.bodyAsText()
        val jsonArray = Json.parseToJsonElement(body).jsonArray
        
        val teachers = jsonArray.map { element ->
            val obj = element.jsonObject
            Teacher(
                id = obj["id"]?.jsonPrimitive?.content ?: "",
                displayName = obj["display_name"]?.jsonPrimitive?.content ?: "Unknown Teacher",
                email = obj["email"]?.jsonPrimitive?.content ?: ""
            )
        }
        
        Log.d("UserRepository", "Fetched ${teachers.size} teachers: ${teachers.map { it.displayName }}")
        Result.success(teachers)
    } catch (e: Exception) {
        Log.e("UserRepository", "Error fetching teachers", e)
        Result.failure(e)
    }
}
```

#### Step 1.3: Update signUp() Function Signature

**Find this (around line 135)**:
```kotlin
suspend fun signUp(
    email: String, 
    password: String, 
    fullName: String = "",
    teacherName: String? = null,  // ❌ OLD
    section: String? = null
): Result<UserInfo>
```

**Replace with**:
```kotlin
suspend fun signUp(
    email: String, 
    password: String, 
    fullName: String = "",
    teacherId: String? = null,    // ✅ NEW: UUID instead of name
    section: String? = null
): Result<UserInfo>
```

#### Step 1.4: Update ensureExtendedProfileAndSettings() Call

**Find this (around line 167)**:
```kotlin
ensureExtendedProfileAndSettings(userInfo.id, displayName, teacherName, section, email)
```

**Replace with**:
```kotlin
ensureExtendedProfileAndSettings(userInfo.id, displayName, teacherId, section, email)
```

#### Step 1.5: Update ensureExtendedProfileAndSettings() Function

**Find this (around line 765)**:
```kotlin
private suspend fun ensureExtendedProfileAndSettings(
    userId: String,
    displayName: String,
    teacherName: String?,  // ❌ OLD
    section: String?,
    email: String
) {
    // ...
    val profileBody = buildJsonObject {
        put("id", userId)
        put("email", email)
        put("full_name", displayName)
        put("display_name", displayName)
        if (!teacherName.isNullOrBlank()) put("teacher_name", teacherName)  // ❌ OLD
        if (!section.isNullOrBlank()) put("section", section)
    }.toString()
    // ...
}
```

**Replace with**:
```kotlin
private suspend fun ensureExtendedProfileAndSettings(
    userId: String,
    displayName: String,
    teacherId: String?,    // ✅ NEW: UUID
    section: String?,
    email: String
) {
    if (userId.isBlank()) return
    val token = accessToken ?: return
    
    try {
        val base = normalizeBaseUrl(supabase.supabaseUrl)
        
        // Create extended profile in profiles table
        val profileUrl = "$base/rest/v1/profiles"
        val profileBody = buildJsonObject {
            put("id", userId)
            put("email", email)
            put("full_name", displayName)
            put("display_name", displayName)
            put("role", "student")  // ✅ NEW: Explicitly set role
            if (!teacherId.isNullOrBlank()) put("teacher_id", teacherId)  // ✅ NEW: FK to teacher
            if (!section.isNullOrBlank()) put("section", section)
        }.toString()
        
        Log.d("UserRepository", "Creating student profile - userId: $userId, email: $email, displayName: $displayName, teacherId: '$teacherId', section: '$section'")
        Log.d("UserRepository", "Profile JSON body: $profileBody")
        
        val profileResp = http.post(profileUrl) {
            header("apikey", supabase.supabaseKey)
            header("Authorization", "Bearer $token")
            header("Prefer", "resolution=merge-duplicates")
            contentType(ContentType.Application.Json)
            setBody(profileBody)
        }
        
        if (profileResp.status.isSuccess()) {
            val responseBody = profileResp.bodyAsText()
            Log.d("UserRepository", "Student profile created for $userId - Response: $responseBody")
        } else {
            val errorBody = profileResp.bodyAsText()
            Log.w("UserRepository", "Profile creation failed: ${profileResp.status} - $errorBody")
        }
        
    } catch (e: Exception) {
        Log.w("UserRepository", "Failed to create profile: ${e.message}")
    }
}
```

**Note**: The `teacher_name` field will be automatically populated by the database trigger when `teacher_id` is set.

---

### Part 2: Update AuthScreen.kt ⏳

**File**: `app/src/main/java/com/example/escape_ar/ui/screens/AuthScreen.kt`

#### Step 2.1: Add State Variables for Teacher Dropdown

**Find this (around line 38)**:
```kotlin
var fullName by remember { mutableStateOf("") }
var teacherName by remember { mutableStateOf("") }  // ❌ OLD
var section by remember { mutableStateOf("") }
```

**Replace with**:
```kotlin
var fullName by remember { mutableStateOf("") }
var section by remember { mutableStateOf("") }

// ✅ NEW: Teacher selection state
var availableTeachers by remember { mutableStateOf<List<UserRepository.Teacher>>(emptyList()) }
var selectedTeacher by remember { mutableStateOf<UserRepository.Teacher?>(null) }
var teachersLoading by remember { mutableStateOf(false) }
var teachersExpanded by remember { mutableStateOf(false) }
```

#### Step 2.2: Add LaunchedEffect to Fetch Teachers

**Add this after the state variables (around line 47)**:
```kotlin
// Fetch teachers when switching to signup mode
LaunchedEffect(isLoginMode) {
    if (!isLoginMode && availableTeachers.isEmpty()) {
        teachersLoading = true
        repo.getAvailableTeachers().fold(
            onSuccess = { teachers ->
                availableTeachers = teachers
                teachersLoading = false
                android.util.Log.d("AuthScreen", "Loaded ${teachers.size} teachers")
            },
            onFailure = { error ->
                errorMessage = "Failed to load teachers: ${error.message}"
                teachersLoading = false
                android.util.Log.e("AuthScreen", "Error loading teachers", error)
            }
        )
    }
}
```

#### Step 2.3: Replace Teacher Name Text Field with Dropdown

**Find this (around line 170-187)**:
```kotlin
// Teacher Name field
OutlinedTextField(
    value = teacherName,
    onValueChange = { teacherName = it },
    label = { Text("Teacher Name (Required)") },
    leadingIcon = {
        Icon(Icons.Default.School, contentDescription = null)
    },
    modifier = Modifier.fillMaxWidth(),
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NeonCyan,
        focusedLabelColor = NeonCyan,
        focusedLeadingIconColor = NeonCyan
    )
)
```

**Replace with**:
```kotlin
// Teacher Selection Dropdown
ExposedDropdownMenuBox(
    expanded = teachersExpanded,
    onExpandedChange = { teachersExpanded = !teachersExpanded }
) {
    OutlinedTextField(
        value = selectedTeacher?.displayName ?: "",
        onValueChange = { },
        readOnly = true,
        label = { Text("Select Your Teacher (Required)") },
        leadingIcon = {
            Icon(Icons.Default.School, contentDescription = null)
        },
        trailingIcon = {
            if (teachersLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = NeonCyan,
                    strokeWidth = 2.dp
                )
            } else {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = teachersExpanded)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            focusedLabelColor = NeonCyan,
            focusedLeadingIconColor = NeonCyan,
            unfocusedBorderColor = ElectricBlue,
            unfocusedLabelColor = ElectricBlue
        )
    )
    
    ExposedDropdownMenu(
        expanded = teachersExpanded,
        onDismissRequest = { teachersExpanded = false },
        modifier = Modifier.background(CharcoalGrey)
    ) {
        if (availableTeachers.isEmpty() && !teachersLoading) {
            DropdownMenuItem(
                text = { Text("No teachers available", color = WhiteSmoke) },
                onClick = { }
            )
        } else {
            availableTeachers.forEach { teacher ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                teacher.displayName,
                                fontWeight = FontWeight.Bold,
                                color = WhiteSmoke
                            )
                            Text(
                                teacher.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = WhiteSmoke.copy(alpha = 0.7f)
                            )
                        }
                    },
                    onClick = {
                        selectedTeacher = teacher
                        teachersExpanded = false
                        android.util.Log.d("AuthScreen", "Selected teacher: ${teacher.displayName} (${teacher.id})")
                    }
                )
            }
        }
    }
}
```

#### Step 2.4: Update Validation Logic

**Find this (around line 278)**:
```kotlin
if (fullName.isBlank() || teacherName.isBlank() || section.isBlank()) {
    errorMessage = "Please fill in all fields"
    return@launch
}
```

**Replace with**:
```kotlin
if (fullName.isBlank() || selectedTeacher == null || section.isBlank()) {
    errorMessage = "Please fill in all fields and select a teacher"
    return@launch
}
```

#### Step 2.5: Update signUp() Call

**Find this (around line 298)**:
```kotlin
repo.signUp(
    email = email,
    password = password,
    fullName = fullName,
    teacherName = teacherName,  // ❌ OLD
    section = section
)
```

**Replace with**:
```kotlin
repo.signUp(
    email = email,
    password = password,
    fullName = fullName,
    teacherId = selectedTeacher?.id,  // ✅ NEW: UUID
    section = section
)
```

---

## 📝 Testing Checklist

### After Making Changes:

1. **Build the app**:
```bash
cd "c:\Users\Jared Dionela\AndroidStudioProjects\ESCAPEAR"
.\gradlew clean installDebug
```

2. **Test Teacher Dropdown**:
   - Open app
   - Go to Sign Up screen
   - Verify dropdown appears with list of teachers
   - Select a teacher
   - Verify teacher name shows in field

3. **Test Student Registration**:
   - Fill out all fields
   - Select a teacher
   - Click Sign Up
   - Verify success

4. **Verify in Database**:
```sql
-- Check if student was created with correct teacher_id
SELECT 
    s.email as student_email,
    s.display_name as student_name,
    s.teacher_id,
    s.teacher_name,
    s.section,
    t.display_name as assigned_teacher
FROM profiles s
LEFT JOIN profiles t ON s.teacher_id = t.id
WHERE s.email = 'teststudent@example.com';
```

5. **Test Web Admin**:
   - Log in as the selected teacher
   - Go to Users page
   - Verify new student appears in the list

---

## 🚨 Common Issues & Solutions

### Issue 1: "No teachers available" in dropdown
**Solution**: 
- Make sure you've created at least one teacher account
- Run: `SELECT * FROM profiles WHERE role = 'teacher';`
- If empty, register a teacher via web-admin `/register`

### Issue 2: Compilation errors
**Solution**:
- Make sure you import `ExposedDropdownMenuBox` and `ExposedDropdownMenu`
- Add to imports: `import androidx.compose.material3.ExposedDropdownMenuBox`
- Add: `import androidx.compose.material3.ExposedDropdownMenu`

### Issue 3: teacher_name is NULL after signup
**Solution**:
- This is normal! The trigger should auto-populate it
- Check database trigger exists:
```sql
SELECT * FROM pg_trigger WHERE tgname = 'trigger_sync_teacher_name';
```

### Issue 4: Student not appearing in teacher's web admin
**Solution**:
- Check student's teacher_id matches the teacher's id
- Check RLS policies are enabled
- Try logging out and back in to refresh

---

## ⏭️ What's Next After Android Updates

1. ✅ Test complete student registration flow
2. ✅ Add "Register Student" button in web-admin (optional)
3. ✅ Test data isolation between multiple teachers
4. ✅ Update documentation
5. ✅ Deploy to production

**Estimated Time**: 2-3 hours for Android updates + testing

Would you like me to make these changes directly in the code files, or would you prefer to implement them yourself using this guide?
