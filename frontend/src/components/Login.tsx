import React, { useState } from 'react';
import {
  Box,
  Paper,
  TextField,
  Button,
  Typography,
  Alert,
  Container,
  CircularProgress,
  FormControlLabel,
  Checkbox,
} from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import axios from 'axios';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const from = location.state?.from?.pathname || '/';

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    const formUsername = username.trim();
    const formPassword = password.trim();

    if (!formUsername || !formPassword) {
      setError('Please enter both username and password');
      setIsLoading(false);
      return;
    }

    try {
      // Use the login function from AuthContext
      const success = await login(formUsername, formPassword);
      
      if (success) {
        navigate(from);
      } else {
        setError('Invalid username or password');
      }
    } catch (error: unknown) {
      console.error('Login error:', error);
      
      if (error && typeof error === 'object' && 'isAxiosError' in error) {
        const axiosError = error as { 
          response?: { 
            status: number; 
            data: { error?: string } 
          }; 
          request?: unknown;
        };
        
        if (axiosError.response) {
          if (axiosError.response.status === 401) {
            setError('Invalid username or password');
          } else if (axiosError.response.status === 429) {
            setError('Too many login attempts. Please try again later.');
          } else {
            setError(`Error: ${axiosError.response.data.error || 'Unknown error'}`);
          }
        } else if (axiosError.request) {
          setError('Network error. Please check your connection.');
        } else {
          setError('An unexpected error occurred');
        }
      } else {
        setError('An unexpected error occurred');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
          <Typography component="h1" variant="h5" gutterBottom>
            Sign in
          </Typography>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}
          <Box 
            component="form" 
            onSubmit={handleSubmit} 
            noValidate
            sx={{ mt: 1 }}
          >
            <TextField
              margin="normal"
              required
              fullWidth
              id="username"
              label="Username"
              name="username"
              autoComplete="username"
              autoFocus
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={isLoading}
              inputProps={{
                'aria-label': 'Username',
              }}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Password"
              type="password"
              id="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={isLoading}
              inputProps={{
                'aria-label': 'Password',
              }}
            />
            <FormControlLabel
              control={
                <Checkbox
                  value="remember"
                  color="primary"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  disabled={isLoading}
                />
              }
              label="Remember me"
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={isLoading}
            >
              {isLoading ? <CircularProgress size={24} /> : 'Sign In'}
            </Button>
          </Box>
          <Typography variant="body2" color="text.secondary" align="center">
            Use admin/admin123 for admin access
            <br />
            or user/user123 for regular access
          </Typography>
        </Paper>
      </Box>
    </Container>
  );
};

export default Login;