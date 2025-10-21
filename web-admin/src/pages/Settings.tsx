import { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  Grid,
  Divider,
  Alert,
  Switch,
  FormControlLabel,
  Card,
  CardContent,
} from '@mui/material';
import {
  Save as SaveIcon,
  Refresh as RefreshIcon,
  Settings as SettingsIcon,
} from '@mui/icons-material';

export function Settings() {
  const [saved, setSaved] = useState(false);
  const [appSettings, setAppSettings] = useState({
    appName: 'E.S.C.A.P.E. AR',
    adminEmail: 'admin@escape-ar.com',
    allowRegistration: true,
    requireEmailVerification: false,
    maxQuizAttempts: 3,
    passingScore: 70,
  });

  const handleSave = () => {
    // In a real app, this would save to database
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  const handleReset = () => {
    setAppSettings({
      appName: 'E.S.C.A.P.E. AR',
      adminEmail: 'admin@escape-ar.com',
      allowRegistration: true,
      requireEmailVerification: false,
      maxQuizAttempts: 3,
      passingScore: 70,
    });
  };

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>
        ⚙️ Settings
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Configure your application settings
      </Typography>

      {saved && (
        <Alert severity="success" sx={{ mb: 3 }}>
          Settings saved successfully!
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* App Configuration */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
              <SettingsIcon color="primary" />
              <Typography variant="h6" fontWeight="bold">
                App Configuration
              </Typography>
            </Box>

            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
              <TextField
                label="App Name"
                value={appSettings.appName}
                onChange={(e) => setAppSettings({ ...appSettings, appName: e.target.value })}
                fullWidth
              />

              <TextField
                label="Admin Email"
                type="email"
                value={appSettings.adminEmail}
                onChange={(e) => setAppSettings({ ...appSettings, adminEmail: e.target.value })}
                fullWidth
                helperText="Primary contact email for the admin"
              />

              <Divider />

              <FormControlLabel
                control={
                  <Switch
                    checked={appSettings.allowRegistration}
                    onChange={(e) =>
                      setAppSettings({ ...appSettings, allowRegistration: e.target.checked })
                    }
                  />
                }
                label="Allow New User Registration"
              />

              <FormControlLabel
                control={
                  <Switch
                    checked={appSettings.requireEmailVerification}
                    onChange={(e) =>
                      setAppSettings({
                        ...appSettings,
                        requireEmailVerification: e.target.checked,
                      })
                    }
                  />
                }
                label="Require Email Verification"
              />
            </Box>
          </Paper>
        </Grid>

        {/* Quiz Settings */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
              <SettingsIcon color="primary" />
              <Typography variant="h6" fontWeight="bold">
                Quiz Settings
              </Typography>
            </Box>

            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
              <TextField
                label="Maximum Quiz Attempts"
                type="number"
                value={appSettings.maxQuizAttempts}
                onChange={(e) =>
                  setAppSettings({ ...appSettings, maxQuizAttempts: parseInt(e.target.value) })
                }
                fullWidth
                helperText="Number of times a student can retake a quiz"
              />

              <TextField
                label="Passing Score (%)"
                type="number"
                value={appSettings.passingScore}
                onChange={(e) =>
                  setAppSettings({ ...appSettings, passingScore: parseInt(e.target.value) })
                }
                fullWidth
                helperText="Minimum score required to pass a quiz"
              />
            </Box>
          </Paper>
        </Grid>

        {/* System Information */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" fontWeight="bold" gutterBottom>
              📋 System Information
            </Typography>
            <Divider sx={{ my: 2 }} />

            <Grid container spacing={2}>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="caption" color="text.secondary">
                      Version
                    </Typography>
                    <Typography variant="h6" fontWeight="bold">
                      1.0.0
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="caption" color="text.secondary">
                      Database
                    </Typography>
                    <Typography variant="h6" fontWeight="bold">
                      Supabase
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="caption" color="text.secondary">
                      Platform
                    </Typography>
                    <Typography variant="h6" fontWeight="bold">
                      Android
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="caption" color="text.secondary">
                      AR Framework
                    </Typography>
                    <Typography variant="h6" fontWeight="bold">
                      Unity
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          </Paper>
        </Grid>

        {/* Action Buttons */}
        <Grid item xs={12}>
          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
            <Button
              variant="outlined"
              startIcon={<RefreshIcon />}
              onClick={handleReset}
            >
              Reset to Default
            </Button>
            <Button
              variant="contained"
              startIcon={<SaveIcon />}
              onClick={handleSave}
            >
              Save Settings
            </Button>
          </Box>
        </Grid>
      </Grid>
    </Box>
  );
}
