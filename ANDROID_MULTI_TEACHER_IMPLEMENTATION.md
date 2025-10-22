# Android App - Multi-Teacher Implementation Guide

## Current Status: ❌ NOT READY

The Android app currently uses a text field for teacher name. It needs to be updated to use a teacher selection dropdown with teacher_id foreign key.

## Required Changes

### 1. Database Migration (Backend - Priority 1)
**Status**: ✅ SQL file created, ⏳ needs to be run in Supabase

**Action**: Run `TEACHER_STUDENT_RELATIONSHIP_MIGRATION.sql` in Supabase Dashboard
- This adds `teacher_id` UUID column to profiles table
- Links students to teachers via foreign key
- Migrates existing data (links students to teachers based on teacher_name)

### 2. UserRepository.kt Updates

#### A. Add API Function to Fetch Available Teachers

**Location**: `app/src/main/java/com/example/escape_ar/data/repository/UserRepository.kt`

Add this function:

```kotlin
data class Teacher(
    val id: String,
    val displayName: String,
    val email: String
)

suspend fun getAvailableTeachers(): Result<List<Teacher>> = withContext(Dispatchers.IO) {
    try {
        val base = normalizeBaseUrl(supabase.supabaseUrl)
        val url = "$base/rest/v1/profiles?role=eq.teacher&select=id,display_name,email"
        
        val response = http.get(url) {
            header("apikey", supabase.supabaseKey)
            header("Accept", "application/json")
        }
        
        if (!response.status.isSuccess()) {
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
                displayName = obj["display_name"]?.jsonPrimitive?.content ?: "Unknown",
                email = obj["email"]?.jsonPrimitive?.content ?: ""
            )
        }
        
        Result.success(teachers)
    } catch (e: Exception) {
        Log.e("UserRepository", "Error fetching teachers", e)
        Result.failure(e)
    }
}
```

#### B. Update signUp Function to Accept teacher_id

**Location**: Same file, update the `signUp` function signature and implementation

**Current**:
```kotlin
suspend fun signUp(
    email: String, 
    password: String, 
    fullName: String = "",
    teacherName: String? = null,  // ❌ Old: text field
    section: String? = null
): Result<UserInfo>
```

**Updated**:
```kotlin
suspend fun signUp(
    email: String, 
    password: String, 
    fullName: String = "",
    teacherId: String? = null,    // ✅ New: UUID foreign key
    section: String? = null
): Result<UserInfo>
```

#### C. Update ensureExtendedProfileAndSettings Function

**Current**:
```kotlin
private suspend fun ensureExtendedProfileAndSettings(
    userId: String,
    displayName: String,
    teacherName: String?,  // ❌ Old
    section: String?,
    email: String
) {
    // ...
    val profileBody = buildJsonObject {
        put("id", userId)
        put("email", email)
        put("full_name", displayName)
        put("display_name", displayName)
        if (!teacherName.isNullOrBlank()) put("teacher_name", teacherName)  // ❌ Old
        if (!section.isNullOrBlank()) put("section", section)
    }.toString()
    // ...
}
```

**Updated**:
```kotlin
private suspend fun ensureExtendedProfileAndSettings(
    userId: String,
    displayName: String,
    teacherId: String?,     // ✅ New: UUID
    section: String?,
    email: String
) {
    // ...
    val profileBody = buildJsonObject {
        put("id", userId)
        put("email", email)
        put("full_name", displayName)
        put("display_name", displayName)
        put("role", "student")  // ✅ Explicitly set role
        if (!teacherId.isNullOrBlank()) put("teacher_id", teacherId)  // ✅ New: FK
        if (!section.isNullOrBlank()) put("section", section)
    }.toString()
    // ...
}
```

**Note**: The `teacher_name` field will be automatically populated by the database trigger `sync_teacher_name()` when `teacher_id` is set.

### 3. AuthScreen.kt Updates

#### A. Replace Teacher Name Text Field with Dropdown

**Location**: `app/src/main/java/com/example/escape_ar/ui/screens/AuthScreen.kt`

**Current (Lines ~170-187)**:
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

**Updated**:
```kotlin
// Teacher Selection Dropdown
var teachersExpanded by remember { mutableStateOf(false) }
var availableTeachers by remember { mutableStateOf<List<UserRepository.Teacher>>(emptyList()) }
var selectedTeacher by remember { mutableStateOf<UserRepository.Teacher?>(null) }
var teachersLoading by remember { mutableStateOf(false) }

// Fetch teachers when switching to signup mode
LaunchedEffect(isLoginMode) {
    if (!isLoginMode && availableTeachers.isEmpty()) {
        teachersLoading = true
        repo.getAvailableTeachers().fold(
            onSuccess = { teachers ->
                availableTeachers = teachers
                teachersLoading = false
            },
            onFailure = { error ->
                errorMessage = "Failed to load teachers: ${error.message}"
                teachersLoading = false
            }
        )
    }
}

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
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
            focusedLeadingIconColor = NeonCyan
        )
    )
    
    ExposedDropdownMenu(
        expanded = teachersExpanded,
        onDismissRequest = { teachersExpanded = false }
    ) {
        availableTeachers.forEach { teacher ->
            DropdownMenuItem(
                text = { 
                    Column {
                        Text(teacher.displayName, fontWeight = FontWeight.Bold)
                        Text(teacher.email, style = MaterialTheme.typography.bodySmall)
                    }
                },
                onClick = {
                    selectedTeacher = teacher
                    teachersExpanded = false
                }
            )
        }
    }
}
```

#### B. Update Signup Button Logic

**Location**: Same file, in the signup button click handler (around line 297)

**Current**:
```kotlin
repo.signUp(
    email = email,
    password = password,
    fullName = fullName,
    teacherName = teacherName,  // ❌ Old: text
    section = section
)
```

**Updated**:
```kotlin
repo.signUp(
    email = email,
    password = password,
    fullName = fullName,
    teacherId = selectedTeacher?.id,  // ✅ New: UUID
    section = section
)
```

#### C. Update Validation

**Current** (around line 278):
```kotlin
if (fullName.isBlank() || teacherName.isBlank() || section.isBlank()) {
    errorMessage = "Please fill in all fields"
    return@launch
}
```

**Updated**:
```kotlin
if (fullName.isBlank() || selectedTeacher == null || section.isBlank()) {
    errorMessage = "Please fill in all fields and select a teacher"
    return@launch
}
```

### 4. Update signUp Call in ensureExtendedProfileAndSettings

**Location**: `UserRepository.kt`, line ~167

**Current**:
```kotlin
ensureExtendedProfileAndSettings(userInfo.id, displayName, teacherName, section, email)
```

**Updated**:
```kotlin
ensureExtendedProfileAndSettings(userInfo.id, displayName, teacherId, section, email)
```

## Implementation Order

### Phase 1: Backend (Do First)
1. ✅ Run `TEACHER_STUDENT_RELATIONSHIP_MIGRATION.sql` in Supabase
2. ✅ Verify migration (check that `teacher_id` column exists)
3. ✅ Create at least 2 teacher accounts for testing

### Phase 2: Android Backend (UserRepository.kt)
1. ✅ Add `Teacher` data class
2. ✅ Add `getAvailableTeachers()` function
3. ✅ Update `signUp()` signature (teacherName → teacherId)
4. ✅ Update `ensureExtendedProfileAndSettings()` (add teacher_id to JSON)

### Phase 3: Android UI (AuthScreen.kt)
1. ✅ Add state variables (availableTeachers, selectedTeacher, etc.)
2. ✅ Add LaunchedEffect to fetch teachers
3. ✅ Replace TextField with ExposedDropdownMenuBox
4. ✅ Update validation logic
5. ✅ Update signup call to pass teacher_id

### Phase 4: Testing
1. ✅ Test teacher dropdown loads correctly
2. ✅ Test student signup with teacher selection
3. ✅ Verify profile created with correct teacher_id in database
4. ✅ Verify teacher_name auto-populated by trigger
5. ✅ Test web admin - verify teacher sees only their students

## Testing Checklist

- [ ] Run database migration in Supabase
- [ ] Create 2 teacher accounts (Teacher A, Teacher B)
- [ ] Build and run Android app
- [ ] Switch to Sign Up mode
- [ ] Verify teacher dropdown appears and loads teachers
- [ ] Register Student 1 with Teacher A
- [ ] Register Student 2 with Teacher B
- [ ] Check database: Verify Student 1 has `teacher_id = Teacher A's ID`
- [ ] Check database: Verify Student 2 has `teacher_id = Teacher B's ID`
- [ ] Check database: Verify `teacher_name` auto-populated from teacher's display_name
- [ ] Log into web admin as Teacher A
- [ ] Verify Teacher A sees only Student 1
- [ ] Log into web admin as Teacher B
- [ ] Verify Teacher B sees only Student 2

## Summary

**Current Status**: ❌ Android app uses text field for teacher name (no foreign key relationship)

**Required Status**: ✅ Android app uses dropdown with teacher_id UUID foreign key

**Estimated Work**: ~2-3 hours
- Backend migration: 15 minutes
- UserRepository updates: 45 minutes
- AuthScreen UI updates: 60-90 minutes
- Testing: 30 minutes

**Blocked By**: Database migration must be run first before testing Android changes
