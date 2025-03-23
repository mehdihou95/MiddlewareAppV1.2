import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  IconButton,
  Alert,
  CircularProgress,
  Chip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Tooltip,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { interfaceService } from '../services/interfaceService';
import { Interface } from '../types';
import { useClientInterface } from '../context/ClientInterfaceContext';
import ClientInterfaceSelector from '../components/ClientInterfaceSelector';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

interface FormData {
  name: string;
  type: string;
  description: string;
  isActive: boolean;
  priority: number;
  rootElement: string;
  namespace: string;
  schemaPath: string;
}

interface FormErrors {
  name: string;
  type: string;
}

const InterfaceManagementPage: React.FC = () => {
  const { selectedClient, refreshInterfaces } = useClientInterface();
  const [interfaces, setInterfaces] = useState<Interface[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [editingInterface, setEditingInterface] = useState<Interface | null>(null);
  const [formData, setFormData] = useState<FormData>({
    name: '',
    type: 'XML',
    description: '',
    isActive: true,
    priority: 0,
    rootElement: '',
    namespace: '',
    schemaPath: ''
  });
  const [formErrors, setFormErrors] = useState<FormErrors>({
    name: '',
    type: '',
  });
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [interfaceToDelete, setInterfaceToDelete] = useState<Interface | null>(null);

  useEffect(() => {
    if (selectedClient) {
      loadInterfaces();
    } else {
      setInterfaces([]);
      setLoading(false);
    }
  }, [selectedClient]);

  const loadInterfaces = async () => {
    if (!selectedClient) return;

    try {
      setLoading(true);
      const data = await interfaceService.getInterfacesByClientId(selectedClient.id);
      setInterfaces(data);
      setError(null);
    } catch (err) {
      setError('Failed to load interfaces');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (interfaceObj?: Interface) => {
    if (interfaceObj) {
      setEditingInterface(interfaceObj);
      setFormData({
        name: interfaceObj.name,
        type: interfaceObj.type,
        description: interfaceObj.description || '',
        isActive: interfaceObj.isActive,
        priority: interfaceObj.priority || 0,
        rootElement: interfaceObj.rootElement || '',
        namespace: interfaceObj.namespace || '',
        schemaPath: interfaceObj.schemaPath || ''
      });
    } else {
      setEditingInterface(null);
      setFormData({
        name: '',
        type: 'XML',
        description: '',
        isActive: true,
        priority: 0,
        rootElement: '',
        namespace: '',
        schemaPath: ''
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setEditingInterface(null);
    setFormData({
      name: '',
      type: 'XML',
      description: '',
      isActive: true,
      priority: 0,
      rootElement: '',
      namespace: '',
      schemaPath: ''
    });
    setFormErrors({
      name: '',
      type: '',
    });
  };

  const validateForm = () => {
    const errors = {
      name: '',
      type: '',
    };
    let isValid = true;

    if (!formData.name.trim()) {
      errors.name = 'Name is required';
      isValid = false;
    }

    if (!formData.type) {
      errors.type = 'Type is required';
      isValid = false;
    }

    setFormErrors(errors);
    return isValid;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm() || !selectedClient) return;

    try {
      const backendInterfaceData = {
        ...formData,
        clientId: selectedClient.id
      };

      if (editingInterface) {
        await interfaceService.updateInterface(editingInterface.id, backendInterfaceData);
      } else {
        await axios.post(
          `${API_URL}/clients/${selectedClient.id}/interfaces`,
          backendInterfaceData,
          {
            withCredentials: true,
            headers: {
              'Content-Type': 'application/json',
              'X-Client-ID': selectedClient.id.toString()
            }
          }
        );
      }
      handleCloseDialog();
      loadInterfaces();
      refreshInterfaces();
    } catch (err) {
      setError('Failed to save interface');
    }
  };

  const handleDeleteClick = (interfaceObj: Interface) => {
    setInterfaceToDelete(interfaceObj);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!interfaceToDelete) return;

    try {
      await interfaceService.deleteInterface(interfaceToDelete.id);
      setDeleteDialogOpen(false);
      setInterfaceToDelete(null);
      loadInterfaces();
      refreshInterfaces();
    } catch (err) {
      setError('Failed to delete interface');
    }
  };

  return (
    <Box sx={{ maxWidth: 1200, mx: 'auto' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Interface Management</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
          disabled={!selectedClient}
        >
          Add Interface
        </Button>
      </Box>

      <ClientInterfaceSelector required />

      {!selectedClient ? (
        <Alert severity="info" sx={{ mt: 2 }}>
          Please select a client to manage interfaces
        </Alert>
      ) : (
        <>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
              <CircularProgress />
            </Box>
          ) : (
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell>Description</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {interfaces.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center">
                        <Typography sx={{ py: 2 }}>
                          No interfaces found for {selectedClient.name}. Click "Add Interface" to create one.
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    interfaces.map((interfaceObj) => (
                      <TableRow key={interfaceObj.id}>
                        <TableCell>{interfaceObj.name}</TableCell>
                        <TableCell>{interfaceObj.type}</TableCell>
                        <TableCell>{interfaceObj.description || '-'}</TableCell>
                        <TableCell>
                          <Chip
                            label={interfaceObj.isActive ? 'Active' : 'Inactive'}
                            color={interfaceObj.isActive ? 'success' : 'default'}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>
                          <Tooltip title="Edit">
                            <IconButton
                              size="small"
                              onClick={() => handleOpenDialog(interfaceObj)}
                            >
                              <EditIcon />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Delete">
                            <IconButton
                              size="small"
                              color="error"
                              onClick={() => handleDeleteClick(interfaceObj)}
                            >
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
          )}

          {/* Create/Edit Dialog */}
          <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
            <form onSubmit={handleSubmit} noValidate>
              <DialogTitle>
                {editingInterface ? 'Edit Interface' : 'Add New Interface'}
              </DialogTitle>
              <DialogContent>
                <Box sx={{ pt: 2 }}>
                  <TextField
                    fullWidth
                    label="Name"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    error={!!formErrors.name}
                    helperText={formErrors.name}
                    sx={{ mb: 2 }}
                  />
                  <FormControl fullWidth error={!!formErrors.type} sx={{ mb: 2 }}>
                    <InputLabel>Type</InputLabel>
                    <Select
                      value={formData.type}
                      label="Type"
                      onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                    >
                      <MenuItem value="XML">XML</MenuItem>
                      <MenuItem value="JSON">JSON</MenuItem>
                      <MenuItem value="CSV">CSV</MenuItem>
                      <MenuItem value="EDI">EDI</MenuItem>
                    </Select>
                    {formErrors.type && (
                      <Typography color="error" variant="caption">
                        {formErrors.type}
                      </Typography>
                    )}
                  </FormControl>
                  <TextField
                    fullWidth
                    label="Description"
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    multiline
                    rows={3}
                    sx={{ mb: 2 }}
                  />
                </Box>
              </DialogContent>
              <DialogActions>
                <Button onClick={handleCloseDialog}>Cancel</Button>
                <Button type="submit" variant="contained">
                  {editingInterface ? 'Update' : 'Create'}
                </Button>
              </DialogActions>
            </form>
          </Dialog>

          {/* Delete Confirmation Dialog */}
          <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
            <DialogTitle>Confirm Delete</DialogTitle>
            <DialogContent>
              <Typography>
                Are you sure you want to delete the interface "{interfaceToDelete?.name}"?
                This action cannot be undone.
              </Typography>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
              <Button onClick={handleDeleteConfirm} color="error" variant="contained">
                Delete
              </Button>
            </DialogActions>
          </Dialog>
        </>
      )}
    </Box>
  );
};

export default InterfaceManagementPage; 