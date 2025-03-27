import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import Container from '@mui/material/Container';
import theme from './theme';
import Navigation from './components/Navigation';
import Login from './components/Login';
import FileUpload from './components/FileUpload';
import ProcessedFiles from './components/ProcessedFiles';
import PrivateRoute from './components/PrivateRoute';
import TransformPage from './pages/TransformPage';
import ClientManagementPage from './pages/ClientManagementPage';
import InterfaceManagementPage from './pages/InterfaceManagementPage';
import { AuthProvider } from './context/AuthContext';
import { ClientInterfaceProvider } from './context/ClientInterfaceContext';
import AuditLogs from './components/AuditLogs';
import UserManagement from './components/UserManagement';
import { useAuth } from './context/AuthContext';
import ErrorBoundary from './components/ErrorBoundary';

// Root component to handle authentication check and default routing
const Root: React.FC = () => {
  const { user, loading } = useAuth();

  if (loading) {
    return null;
  }

  if (!user || !user.authenticated) {
    return <Navigate to="/login" replace />;
  }

  // Redirect authenticated users to the clients page as the main dashboard
  return <Navigate to="/clients" replace />;
};

// AppContent component that uses auth context
const AppContent: React.FC = () => {
  const { user } = useAuth();

  return (
    <>
      {/* Only show Navigation if user is authenticated */}
      {user?.authenticated && <Navigation />}
      <Container sx={{ mt: 4 }}>
        <Routes>
          {/* Public route */}
          <Route 
            path="/login" 
            element={
              user?.authenticated ? <Navigate to="/" replace /> : <Login />
            } 
          />
          
          {/* Protected routes */}
          <Route
            path="/"
            element={
              <PrivateRoute>
                <Root />
              </PrivateRoute>
            }
          />
          <Route
            path="/history"
            element={
              <PrivateRoute>
                <ProcessedFiles />
              </PrivateRoute>
            }
          />
          <Route
            path="/transform"
            element={
              <PrivateRoute>
                <TransformPage />
              </PrivateRoute>
            }
          />
          <Route
            path="/clients"
            element={
              <PrivateRoute>
                <ClientManagementPage />
              </PrivateRoute>
            }
          />
          <Route
            path="/interfaces"
            element={
              <PrivateRoute>
                <InterfaceManagementPage />
              </PrivateRoute>
            }
          />
          <Route 
            path="/audit-logs" 
            element={
              <PrivateRoute>
                <AuditLogs />
              </PrivateRoute>
            } 
          />
          <Route 
            path="/users" 
            element={
              <PrivateRoute>
                <UserManagement />
              </PrivateRoute>
            } 
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Container>
    </>
  );
};

// Main App component
const App: React.FC = () => {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <Router>
            <ClientInterfaceProvider>
              <AppContent />
            </ClientInterfaceProvider>
          </Router>
        </ThemeProvider>
      </AuthProvider>
    </ErrorBoundary>
  );
};

export default App; 