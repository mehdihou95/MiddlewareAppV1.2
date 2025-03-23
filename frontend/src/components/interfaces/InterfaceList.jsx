import React, { useState, useEffect } from 'react';
import { 
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Paper, Button, Typography, Box, Chip, IconButton, Tooltip,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField,
  MenuItem, FormControlLabel, Switch
} from '@mui/material';
import { Edit as EditIcon, Delete as DeleteIcon, Add as AddIcon, 
  Visibility as ViewIcon, Settings as SettingsIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { interfaceService } from '../../services/interfaceService';
import { useSnackbar } from 'notistack';

const InterfaceList = () => {
  const [interfaces, setInterfaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [selectedInterface, setSelectedInterface] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    type: '',
    description: '',
    schemaPath: '',
    rootElement: '',
    namespace: '',
    isActive: true,
    priority: 0
  });
  
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  
  useEffect(() => {
    loadInterfaces();
  }, []);
  
  const loadInterfaces = async () => {
    try {
      setLoading(true);
      const data = await interfaceService.getAllInterfaces();
      setInterfaces(data);
      setError(null);
    } catch (err) {
      setError('Failed to load interfaces: ' + err.message);
      enqueueSnackbar('Failed to load interfaces', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };
  
  const handleOpenDialog = (interfaceData = null) => {
    if (interfaceData) {
      setFormData(interfaceData);
      setSelectedInterface(interfaceData);
    } else {
      setFormData({
        name: '',
        type: '',
        description: '',
        schemaPath: '',
        rootElement: '',
        namespace: '',
        isActive: true,
        priority: 0
      });
      setSelectedInterface(null);
    }
    setOpenDialog(true);
  };
  
  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedInterface(null);
    setFormData({
      name: '',
      type: '',
      description: '',
      schemaPath: '',
      rootElement: '',
      namespace: '',
      isActive: true,
      priority: 0
    });
  };
  
  const handleInputChange = (e) => {
    const { name, value, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'isActive' ? checked : value
    }));
  };
  
  const handleSubmit = async () => {
    try {
      if (selectedInterface) {
        await interfaceService.updateInterface(selectedInterface.id, formData);
        enqueueSnackbar('Interface updated successfully', { variant: 'success' });
      } else {
        await interfaceService.createInterface(formData);
        enqueueSnackbar('Interface created successfully', { variant: 'success' });
      }
      handleCloseDialog();
      loadInterfaces();
    } catch (err) {
      enqueueSnackbar('Failed to save interface: ' + err.message, { variant: 'error' });
    }
  };
  
  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this interface?')) {
      try {
        await interfaceService.deleteInterface(id);
        enqueueSnackbar('Interface deleted successfully', { variant: 'success' });
        loadInterfaces();
      } catch (err) {
        enqueueSnackbar('Failed to delete interface: ' + err.message, { variant: 'error' });
      }
    }
  };
  
  const handleViewMappings = (id) => {
    navigate(`/interfaces/${id}/mappings`);
  };
  
  if (loading) return <Typography>Loading interfaces...</Typography>;
  if (error) return <Typography color="error">{error}</Typography>;
  
  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h5">Interface Management</Typography>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          Create New Interface
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Root Element</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Priority</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {interfaces.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  No interfaces found. Create your first interface to get started.
                </TableCell>
              </TableRow>
            ) : (
              interfaces.map((intf) => (
                <TableRow key={intf.id}>
                  <TableCell>{intf.name}</TableCell>
                  <TableCell>{intf.type}</TableCell>
                  <TableCell>{intf.rootElement}</TableCell>
                  <TableCell>
                    <Chip 
                      label={intf.isActive ? "Active" : "Inactive"} 
                      color={intf.isActive ? "success" : "default"} 
                      size="small" 
                    />
                  </TableCell>
                  <TableCell>{intf.priority}</TableCell>
                  <TableCell>
                    <Tooltip title="View Mappings">
                      <IconButton onClick={() => handleViewMappings(intf.id)}>
                        <ViewIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Edit">
                      <IconButton onClick={() => handleOpenDialog(intf)}>
                        <EditIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Delete">
                      <IconButton onClick={() => handleDelete(intf.id)} color="error">
                        <DeleteIcon />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          {selectedInterface ? 'Edit Interface' : 'Create New Interface'}
        </DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={2}>
            <TextField
              label="Name"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              fullWidth
              required
            />
            <TextField
              label="Type"
              name="type"
              value={formData.type}
              onChange={handleInputChange}
              fullWidth
              required
            />
            <TextField
              label="Description"
              name="description"
              value={formData.description}
              onChange={handleInputChange}
              fullWidth
              multiline
              rows={2}
            />
            <TextField
              label="Schema Path"
              name="schemaPath"
              value={formData.schemaPath}
              onChange={handleInputChange}
              fullWidth
            />
            <TextField
              label="Root Element"
              name="rootElement"
              value={formData.rootElement}
              onChange={handleInputChange}
              fullWidth
              required
            />
            <TextField
              label="Namespace"
              name="namespace"
              value={formData.namespace}
              onChange={handleInputChange}
              fullWidth
            />
            <TextField
              label="Priority"
              name="priority"
              type="number"
              value={formData.priority}
              onChange={handleInputChange}
              fullWidth
            />
            <FormControlLabel
              control={
                <Switch
                  name="isActive"
                  checked={formData.isActive}
                  onChange={handleInputChange}
                />
              }
              label="Active"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button onClick={handleSubmit} variant="contained" color="primary">
            {selectedInterface ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default InterfaceList; 