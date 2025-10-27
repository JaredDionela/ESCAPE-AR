-- ====================================================================
-- Complete User Deletion Function
-- Allows teachers to completely delete their students including auth
-- ====================================================================

-- Create a function to delete user from auth.users (requires elevated privileges)
-- This function will be executed with SECURITY DEFINER (runs with function owner's privileges)
CREATE OR REPLACE FUNCTION delete_user_completely(user_id UUID)
RETURNS JSON
LANGUAGE plpgsql
SECURITY DEFINER -- Run with elevated privileges
SET search_path = public
AS $$
DECLARE
  v_current_user_id UUID;
  v_current_user_role TEXT;
  v_target_user_teacher_id UUID;
  v_result JSON;
BEGIN
  -- Get current authenticated user
  v_current_user_id := auth.uid();
  
  IF v_current_user_id IS NULL THEN
    RAISE EXCEPTION 'Not authenticated';
  END IF;
  
  -- Get current user's role
  SELECT role INTO v_current_user_role
  FROM profiles
  WHERE id = v_current_user_id;
  
  -- Get target user's teacher_id
  SELECT teacher_id INTO v_target_user_teacher_id
  FROM profiles
  WHERE id = user_id;
  
  -- Authorization check: Only allow if:
  -- 1. Current user is the teacher of the target student
  IF v_current_user_role = 'teacher' AND v_target_user_teacher_id = v_current_user_id THEN
    -- Authorized - proceed with deletion
    
    -- Delete quiz results
    DELETE FROM quiz_results WHERE user_id = user_id;
    
    -- Delete progress
    DELETE FROM progress WHERE user_id = user_id;
    
    -- Delete profile
    DELETE FROM profiles WHERE id = user_id;
    
    -- Delete from auth.users (this requires SECURITY DEFINER)
    DELETE FROM auth.users WHERE id = user_id;
    
    v_result := json_build_object(
      'success', true,
      'message', 'User completely deleted',
      'user_id', user_id
    );
    
    RETURN v_result;
  ELSE
    RAISE EXCEPTION 'Not authorized to delete this user';
  END IF;
  
EXCEPTION
  WHEN OTHERS THEN
    RAISE EXCEPTION 'Error deleting user: %', SQLERRM;
END;
$$;

-- Grant execute permission to authenticated users (teachers)
GRANT EXECUTE ON FUNCTION delete_user_completely(UUID) TO authenticated;

-- Add helpful comment
COMMENT ON FUNCTION delete_user_completely(UUID) IS 
  'Completely deletes a student user including auth account. Only teachers can delete their own students.';

-- ====================================================================
-- VERIFICATION
-- ====================================================================

SELECT 
  '=== FUNCTION CREATED SUCCESSFULLY ===' as info;

-- Show function details
SELECT 
  proname as function_name,
  prosecdef as is_security_definer,
  provolatile as volatility,
  pg_get_functiondef(oid) as definition
FROM pg_proc
WHERE proname = 'delete_user_completely';

-- ====================================================================
-- USAGE INSTRUCTIONS
-- ====================================================================

/*
To use this function in your web admin, call it via RPC:

```typescript
// In web-admin/src/lib/api/users.ts

export async function deleteUser(id: string) {
  try {
    // Call the RPC function to completely delete user
    const { data, error } = await supabase.rpc('delete_user_completely', {
      user_id: id
    })
    
    if (error) throw error
    
    console.log('User deleted:', data)
    return data
  } catch (error) {
    console.error('Error deleting user:', error)
    throw error
  }
}
```

This will:
1. ✅ Delete all quiz_results
2. ✅ Delete all progress records
3. ✅ Delete the profile
4. ✅ Delete the auth.users account
5. ✅ Verify teacher has permission

*/

-- ====================================================================
-- DONE! ✅
-- Function created successfully
-- Teachers can now completely delete their students
-- ====================================================================
