import React, { useState } from 'react';
import { Link as RouterLink, useNavigate, useLocation } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Button, Box, Divider, IconButton, Menu, MenuItem, Chip, ListItem, ListItemIcon, ListItemText, List } from '@mui/material';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import LoginIcon from '@mui/icons-material/Login';
import LogoutIcon from '@mui/icons-material/Logout';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import BrushIcon from '@mui/icons-material/Brush';
import TransformIcon from '@mui/icons-material/Transform';
import AccountCircle from '@mui/icons-material/AccountCircle';
import { useAuth } from '../context/AuthContext';
import HistoryIcon from '@mui/icons-material/History';
import BusinessIcon from '@mui/icons-material/Business';
import SettingsEthernetIcon from '@mui/icons-material/SettingsEthernet';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import { useClientInterface } from '../context/ClientInterfaceContext';
import { People as PeopleIcon } from '@mui/icons-material';

const Navigation = () => {
  const location = useLocation();
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { 
    clients, 
    interfaces, 
    selectedClient, 
    selectedInterface,
    setSelectedClient,
    setSelectedInterface
  } = useClientInterface();
  
  const [clientMenuAnchor, setClientMenuAnchor] = useState<null | HTMLElement>(null);
  const [interfaceMenuAnchor, setInterfaceMenuAnchor] = useState<null | HTMLElement>(null);
  const [userMenuAnchor, setUserMenuAnchor] = useState<null | HTMLElement>(null);

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleClientMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setClientMenuAnchor(event.currentTarget);
  };

  const handleClientMenuClose = () => {
    setClientMenuAnchor(null);
  };

  const handleInterfaceMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setInterfaceMenuAnchor(event.currentTarget);
  };

  const handleInterfaceMenuClose = () => {
    setInterfaceMenuAnchor(null);
  };

  const handleUserMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setUserMenuAnchor(event.currentTarget);
  };

  const handleUserMenuClose = () => {
    setUserMenuAnchor(null);
  };

  const handleClientSelect = (client: any) => {
    setSelectedClient(client);
    handleClientMenuClose();
  };

  const handleInterfaceSelect = (interfaceObj: any) => {
    setSelectedInterface(interfaceObj);
    handleInterfaceMenuClose();
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login');
    handleUserMenuClose();
  };

  const isActive = (path: string) => {
    return location.pathname === path;
  };

  if (!user?.authenticated) {
    return (
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            XML Processor
          </Typography>
          <Button
            color="inherit"
            component={RouterLink}
            to="/login"
            startIcon={<LoginIcon />}
          >
            Login
          </Button>
        </Toolbar>
      </AppBar>
    );
  }

  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
          XML Processor
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          {/* File Processing Section */}
          <Button
            color="inherit"
            component={RouterLink}
            to="/"
            startIcon={<UploadFileIcon />}
            sx={{ mr: 2 }}
          >
            Upload
          </Button>
          <Button
            color="inherit"
            component={RouterLink}
            to="/history"
            startIcon={<HistoryIcon />}
            sx={{ mr: 2 }}
          >
            History
          </Button>
          <Button
            color="inherit"
            component={RouterLink}
            to="/transform"
            startIcon={<TransformIcon />}
            sx={{ mr: 2 }}
          >
            Transform
          </Button>

          <Divider orientation="vertical" flexItem sx={{ bgcolor: 'white' }} />

          {/* Admin Section */}
          {user.roles.includes('ADMIN') && (
            <Button
              color="inherit"
              component={RouterLink}
              to="/admin"
              startIcon={<AdminPanelSettingsIcon />}
            >
              Admin
            </Button>
          )}
          <Button
            color="inherit"
            component={RouterLink}
            to="/ux"
            startIcon={<BrushIcon />}
          >
            UX
          </Button>

          <Divider orientation="vertical" flexItem sx={{ bgcolor: 'white' }} />

          <Box sx={{ display: 'flex', alignItems: 'center', mr: 2 }}>
            <Button
              color="inherit"
              component={RouterLink}
              to="/clients"
              sx={{ 
                mx: 1,
                fontWeight: isActive('/clients') ? 'bold' : 'normal',
                borderBottom: isActive('/clients') ? '2px solid white' : 'none'
              }}
            >
              Clients
            </Button>
            <Button
              color="inherit"
              component={RouterLink}
              to="/interfaces"
              sx={{ 
                mx: 1,
                fontWeight: isActive('/interfaces') ? 'bold' : 'normal',
                borderBottom: isActive('/interfaces') ? '2px solid white' : 'none'
              }}
            >
              Interfaces
            </Button>
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', mr: 2 }}>
            <Chip
              icon={<BusinessIcon />}
              label={selectedClient ? selectedClient.name : "Select Client"}
              onClick={handleClientMenuOpen}
              color={selectedClient ? "primary" : "default"}
              variant="outlined"
              sx={{ mr: 1, bgcolor: 'rgba(255,255,255,0.1)' }}
              deleteIcon={<KeyboardArrowDownIcon />}
              onDelete={handleClientMenuOpen}
            />
            
            <Menu
              anchorEl={clientMenuAnchor}
              open={Boolean(clientMenuAnchor)}
              onClose={handleClientMenuClose}
            >
              {!clients || clients.length === 0 ? (
                <MenuItem disabled>
                  No clients available
                </MenuItem>
              ) : (
                clients.map(client => (
                  <MenuItem 
                    key={client.id} 
                    onClick={() => handleClientSelect(client)}
                    selected={selectedClient?.id === client.id}
                  >
                    {client.name}
                  </MenuItem>
                ))
              )}
              <Divider />
              <MenuItem 
                component={RouterLink} 
                to="/clients"
                onClick={handleClientMenuClose}
              >
                Manage Clients
              </MenuItem>
            </Menu>

            {selectedClient && (
              <>
                <Chip
                  icon={<SettingsEthernetIcon />}
                  label={selectedInterface ? selectedInterface.name : "Select Interface"}
                  onClick={handleInterfaceMenuOpen}
                  color={selectedInterface ? "primary" : "default"}
                  variant="outlined"
                  sx={{ bgcolor: 'rgba(255,255,255,0.1)' }}
                  deleteIcon={<KeyboardArrowDownIcon />}
                  onDelete={handleInterfaceMenuOpen}
                />
                
                <Menu
                  anchorEl={interfaceMenuAnchor}
                  open={Boolean(interfaceMenuAnchor)}
                  onClose={handleInterfaceMenuClose}
                >
                  {interfaces.length === 0 ? (
                    <MenuItem disabled>
                      No interfaces available for this client
                    </MenuItem>
                  ) : (
                    interfaces.map(interfaceObj => (
                      <MenuItem 
                        key={interfaceObj.id} 
                        onClick={() => handleInterfaceSelect(interfaceObj)}
                        selected={selectedInterface?.id === interfaceObj.id}
                      >
                        {interfaceObj.name}
                      </MenuItem>
                    ))
                  )}
                  <Divider />
                  <MenuItem 
                    component={RouterLink} 
                    to="/interfaces"
                    onClick={handleInterfaceMenuClose}
                  >
                    Manage Interfaces
                  </MenuItem>
                </Menu>
              </>
            )}
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <IconButton 
              color="inherit" 
              sx={{ mr: 1 }}
              onClick={handleUserMenuOpen}
            >
              <AccountCircle />
            </IconButton>
            <Typography 
              variant="body2" 
              sx={{ mr: 1, cursor: 'pointer' }}
              onClick={handleUserMenuOpen}
            >
              {user.username}
            </Typography>
            
            <Menu
              anchorEl={userMenuAnchor}
              open={Boolean(userMenuAnchor)}
              onClose={handleUserMenuClose}
            >
              <MenuItem onClick={handleUserMenuClose}>Profile</MenuItem>
              <MenuItem onClick={handleUserMenuClose}>Settings</MenuItem>
              <Divider />
              <MenuItem onClick={handleLogout}>Logout</MenuItem>
            </Menu>
          </Box>
        </Box>
        <List>
          <ListItem button component={RouterLink} to="/audit-logs">
            <ListItemIcon>
              <HistoryIcon />
            </ListItemIcon>
            <ListItemText primary="Audit Logs" />
          </ListItem>
          <ListItem button component={RouterLink} to="/users">
            <ListItemIcon>
              <PeopleIcon />
            </ListItemIcon>
            <ListItemText primary="User Management" />
          </ListItem>
        </List>
      </Toolbar>
    </AppBar>
  );
};

export default Navigation; 