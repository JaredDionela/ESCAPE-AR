import { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  CircularProgress,
  Alert,
  List,
  ListItem,
  ListItemText,
  Divider,
  Chip,
} from '@mui/material';
import { CheckCircle, Error as ErrorIcon, Info } from '@mui/icons-material';
import { supabase } from '../lib/supabase';

interface TestResult {
  name: string;
  status: 'pending' | 'success' | 'error' | 'info';
  message: string;
  details?: any;
}

export function DiagnosticTest() {
  const [running, setRunning] = useState(false);
  const [results, setResults] = useState<TestResult[]>([]);

  const addResult = (result: TestResult) => {
    setResults((prev) => [...prev, result]);
  };

  const runDiagnostics = async () => {
    setRunning(true);
    setResults([]);

    try {
      // Test 1: Check Supabase Connection
      addResult({ name: 'Supabase Connection', status: 'pending', message: 'Testing...' });
      try {
        const { error } = await supabase.from('profiles').select('count').limit(1);
        if (error) throw error;
        addResult({
          name: 'Supabase Connection',
          status: 'success',
          message: 'Connected successfully',
        });
      } catch (err: any) {
        addResult({
          name: 'Supabase Connection',
          status: 'error',
          message: `Failed: ${err.message}`,
        });
      }

      // Test 2: Check Lessons Table
      addResult({ name: 'Lessons Table', status: 'pending', message: 'Testing...' });
      try {
        const { data, error, count } = await supabase
          .from('lessons')
          .select('*', { count: 'exact', head: false });
        if (error) throw error;
        addResult({
          name: 'Lessons Table',
          status: 'success',
          message: `Found ${count || 0} lessons`,
          details: data?.slice(0, 3),
        });
      } catch (err: any) {
        addResult({
          name: 'Lessons Table',
          status: 'error',
          message: `Failed: ${err.message}`,
        });
      }

      // Test 3: Check Quiz Questions Table
      addResult({ name: 'Quiz Questions Table', status: 'pending', message: 'Testing...' });
      try {
        const { data, error, count } = await supabase
          .from('quiz_questions')
          .select('*', { count: 'exact', head: false });
        if (error) throw error;
        addResult({
          name: 'Quiz Questions Table',
          status: 'success',
          message: `Found ${count || 0} quiz questions`,
          details: data?.slice(0, 3),
        });
      } catch (err: any) {
        addResult({
          name: 'Quiz Questions Table',
          status: 'error',
          message: `Failed: ${err.message}`,
        });
      }

      // Test 4: Check Storage Access
      addResult({ name: 'Storage Access', status: 'pending', message: 'Testing...' });
      try {
        const { data, error } = await supabase.storage.listBuckets();
        if (error) throw error;
        const bucketNames = data.map((b) => b.name).join(', ');
        addResult({
          name: 'Storage Access',
          status: 'success',
          message: `Found ${data.length} buckets: ${bucketNames}`,
          details: data,
        });
      } catch (err: any) {
        addResult({
          name: 'Storage Access',
          status: 'error',
          message: `Failed: ${err.message}`,
        });
      }

      // Test 5: Check lesson-files bucket
      addResult({ name: 'Lesson Files Bucket', status: 'pending', message: 'Testing...' });
      try {
        const { data, error } = await supabase.storage.from('lesson-files').list();
        if (error) throw error;
        addResult({
          name: 'Lesson Files Bucket',
          status: 'success',
          message: `Bucket accessible, ${data.length} items at root`,
        });
      } catch (err: any) {
        addResult({
          name: 'Lesson Files Bucket',
          status: 'error',
          message: `Failed: ${err.message}`,
        });
      }

      // Test 6: Test Lesson Creation (dry run - we'll insert and delete)
      addResult({ name: 'Lesson Create Test', status: 'pending', message: 'Testing...' });
      try {
        const testLesson = {
          module_id: 'decantation',
          title: '[TEST] Diagnostic Test Lesson',
          description: 'This is a test lesson created by diagnostics',
          youtube_video_id: 'dQw4w9WgXcQ',
          order_index: 999,
          duration_minutes: 0,
        };

        const { data: created, error: createError } = await supabase
          .from('lessons')
          .insert([testLesson])
          .select()
          .single();

        if (createError) throw createError;

        // Delete the test lesson
        const { error: deleteError } = await supabase
          .from('lessons')
          .delete()
          .eq('id', created.id);

        if (deleteError) {
          addResult({
            name: 'Lesson Create Test',
            status: 'info',
            message: `Created lesson successfully but failed to delete: ${deleteError.message}`,
          });
        } else {
          addResult({
            name: 'Lesson Create Test',
            status: 'success',
            message: 'Lesson creation and deletion works correctly',
          });
        }
      } catch (err: any) {
        addResult({
          name: 'Lesson Create Test',
          status: 'error',
          message: `Failed: ${err.message}`,
        });
      }

      // Test 7: Test Quiz Question Creation
      addResult({ name: 'Quiz Create Test', status: 'pending', message: 'Testing...' });
      try {
        const testQuestion = {
          module_id: 'decantation',
          question_text: '[TEST] This is a diagnostic test question',
          option_a: 'Option A',
          option_b: 'Option B',
          option_c: 'Option C',
          option_d: 'Option D',
          correct_answer: 'A',
          order_index: 999,
        };

        const { data: created, error: createError } = await supabase
          .from('quiz_questions')
          .insert([testQuestion])
          .select()
          .single();

        if (createError) throw createError;

        // Delete the test question
        const { error: deleteError } = await supabase
          .from('quiz_questions')
          .delete()
          .eq('id', created.id);

        if (deleteError) {
          addResult({
            name: 'Quiz Create Test',
            status: 'info',
            message: `Created quiz successfully but failed to delete: ${deleteError.message}`,
          });
        } else {
          addResult({
            name: 'Quiz Create Test',
            status: 'success',
            message: 'Quiz question creation and deletion works correctly',
          });
        }
      } catch (err: any) {
        addResult({
          name: 'Quiz Create Test',
          status: 'error',
          message: `Failed: ${err.message}`,
        });
      }

      // Test 8: Check Profiles
      addResult({ name: 'Profiles Table', status: 'pending', message: 'Testing...' });
      try {
        const { error, count } = await supabase
          .from('profiles')
          .select('*', { count: 'exact', head: true });
        if (error) throw error;
        addResult({
          name: 'Profiles Table',
          status: 'success',
          message: `Found ${count || 0} user profiles`,
        });
      } catch (err: any) {
        addResult({
          name: 'Profiles Table',
          status: 'error',
          message: `Failed: ${err.message}`,
        });
      }

      // Summary
      const finalResults = results.filter((r) => r.status !== 'pending');
      const successCount = finalResults.filter((r) => r.status === 'success').length;
      const errorCount = finalResults.filter((r) => r.status === 'error').length;

      addResult({
        name: 'Diagnostic Summary',
        status: errorCount === 0 ? 'success' : 'info',
        message: `Completed: ${successCount} passed, ${errorCount} failed`,
      });
    } catch (err) {
      addResult({
        name: 'Diagnostic Error',
        status: 'error',
        message: `Unexpected error: ${err}`,
      });
    } finally {
      setRunning(false);
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'success':
        return <CheckCircle color="success" />;
      case 'error':
        return <ErrorIcon color="error" />;
      case 'info':
        return <Info color="info" />;
      default:
        return <CircularProgress size={20} />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'success':
        return 'success';
      case 'error':
        return 'error';
      case 'info':
        return 'info';
      default:
        return 'default';
    }
  };

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>
        🔧 System Diagnostics
      </Typography>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="body1" paragraph>
          This diagnostic tool tests all critical functionalities of the admin panel including
          database connectivity, storage access, and CRUD operations.
        </Typography>

        <Button
          variant="contained"
          onClick={runDiagnostics}
          disabled={running}
          startIcon={running ? <CircularProgress size={20} /> : null}
        >
          {running ? 'Running Diagnostics...' : 'Run Diagnostics'}
        </Button>
      </Paper>

      {results.length > 0 && (
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Test Results
          </Typography>
          <List>
            {results.map((result, index) => (
              <Box key={index}>
                <ListItem>
                  <Box sx={{ display: 'flex', alignItems: 'center', width: '100%', gap: 2 }}>
                    {getStatusIcon(result.status)}
                    <Box sx={{ flex: 1 }}>
                      <ListItemText
                        primary={result.name}
                        secondary={result.message}
                        primaryTypographyProps={{ fontWeight: 'bold' }}
                      />
                      {result.details && (
                        <Box sx={{ mt: 1 }}>
                          <Chip
                            label="View Details"
                            size="small"
                            onClick={() =>
                              console.log(`${result.name} details:`, result.details)
                            }
                          />
                          <Typography variant="caption" display="block" sx={{ mt: 1 }}>
                            (Check browser console for details)
                          </Typography>
                        </Box>
                      )}
                    </Box>
                    <Chip
                      label={result.status}
                      color={getStatusColor(result.status) as any}
                      size="small"
                    />
                  </Box>
                </ListItem>
                {index < results.length - 1 && <Divider />}
              </Box>
            ))}
          </List>
        </Paper>
      )}

      {!running && results.length === 0 && (
        <Alert severity="info">
          Click "Run Diagnostics" to test the system functionality
        </Alert>
      )}
    </Box>
  );
}
