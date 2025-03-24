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
  TablePagination,
  TableSortLabel,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { clientService } from '../services/clientService';
import { Client, ClientInput } from '../types';
import { useClientInterface } from '../context/ClientInterfaceContext';

type Order = 'asc' | 'desc';

const ClientManagementPage: React.FC = () => {
  const { refreshClients } = useClientInterface();
  const [clients, setClients] = useState<Client[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [editingClient, setEditingClient] = useState<Client | null>(null);
  const [formData, setFormData] = useState<ClientInput>({
    name: '',
    code: '',
    description: '',
    status: 'ACTIVE',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  });
  const [formErrors, setFormErrors] = useState({
    name: '',
    code: '',
    status: '',
  });
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [clientToDelete, setClientToDelete] = useState<Client | null>(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalCount, setTotalCount] = useState(0);
  const [orderBy, setOrderBy] = useState<keyof Client>('name');
  const [order, setOrder] = useState<Order>('asc');

  useEffect(() => {
    loadClients();
  }, [page, rowsPerPage, orderBy, order]);

  const loadClients = async () => {
    try {
      setLoading(true);
      const response = await clientService.getAllClients(
        page,
        rowsPerPage,
        orderBy,
        order
      );
      
      setClients(response.content);
      setTotalCount(response.totalElements);
      setError(null);
    } catch (err) {
      const axiosError = err as any;
      setError(axiosError.response?.data?.message || 'Failed to load clients');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (client?: Client) => {
    if (client) {
      setEditingClient(client);
      setFormData({
        name: client.name,
        code: client.code,
        description: client.description || '',
        status: client.status,
        createdAt: client.createdAt,
        updatedAt: new Date().toISOString()
      });
    } else {
      setEditingClient(null);
      setFormData({
        name: '',
        code: '',
        description: '',
        status: 'ACTIVE',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setEditingClient(null);
    setFormData({
      name: '',
      code: '',
      description: '',
      status: 'ACTIVE',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    });
    setFormErrors({
      name: '',
      code: '',
      status: '',
    });
  };

  const validateForm = () => {
    const errors = {
      name: '',
      code: '',
      status: '',
    };
    let isValid = true;

    if (!formData.name.trim()) {
      errors.name = 'Name is required';
      isValid = false;
    }

    if (!formData.code.trim()) {
      errors.code = 'Code is required';
      isValid = false;
    } else if (!/^[A-Z0-9-_]+$/.test(formData.code.trim().toUpperCase())) {
      errors.code = 'Code must contain only letters, numbers, hyphens, and underscores';
      isValid = false;
    }

    if (!formData.status) {
      errors.status = 'Status is required';
      isValid = false;
    }

    setFormErrors(errors);
    return isValid;
  };

  const handleSubmit = async () => {
    if (!validateForm()) return;

    try {
      const clientData = {
        ...formData,
        code: formData.code.trim().toUpperCase(),
      };

      if (editingClient) {
        await clientService.updateClient(editingClient.id, clientData);
      } else {
        await clientService.createClient(clientData);
      }
      handleCloseDialog();
      loadClients();
      refreshClients();
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to save client';
      setError(errorMessage);
      if (err.response?.status === 400 && err.response?.data?.message?.includes('code')) {
        setFormErrors(prev => ({
          ...prev,
          code: 'This code is already in use'
        }));
      }
    }
  };

  const handleDeleteClick = (client: Client) => {
    setClientToDelete(client);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!clientToDelete) return;

    try {
      await clientService.deleteClient(clientToDelete.id);
      setDeleteDialogOpen(false);
      setClientToDelete(null);
      loadClients();
      refreshClients();
    } catch (err) {
      const axiosError = err as any;
      setError(axiosError.response?.data?.message || 'Failed to delete client');
    }
  };

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleRequestSort = (property: keyof Client) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
  };

  return (
    <Box sx={{ maxWidth: 1200, mx: 'auto', p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Client Management</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          Add Client
        </Button>
      </Box>

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
        <>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>
                    <TableSortLabel
                      active={orderBy === 'name'}
                      direction={orderBy === 'name' ? order : 'asc'}
                      onClick={() => handleRequestSort('name')}
                    >
                      Name
                    </TableSortLabel>
                  </TableCell>
                  <TableCell>
                    <TableSortLabel
                      active={orderBy === 'code'}
                      direction={orderBy === 'code' ? order : 'asc'}
                      onClick={() => handleRequestSort('code')}
                    >
                      Code
                    </TableSortLabel>
                  </TableCell>
                  <TableCell>Description</TableCell>
                  <TableCell>
                    <TableSortLabel
                      active={orderBy === 'status'}
                      direction={orderBy === 'status' ? order : 'asc'}
                      onClick={() => handleRequestSort('status')}
                    >
                      Status
                    </TableSortLabel>
                  </TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(!clients || clients.length === 0) ? (
                  <TableRow>
                    <TableCell colSpan={5} align="center">
                      <Typography sx={{ py: 2 }}>
                        No clients found
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  clients.map((client) => (
                    <TableRow key={client.id}>
                      <TableCell>{client.name}</TableCell>
                      <TableCell>{client.code}</TableCell>
                      <TableCell>{client.description || 'N/A'}</TableCell>
                      <TableCell>
                        <Chip
                          label={client.status}
                          color={client.status === 'ACTIVE' ? 'success' : 'default'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Tooltip title="Edit">
                          <IconButton onClick={() => handleOpenDialog(client)}>
                            <EditIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Delete">
                          <IconButton onClick={() => handleDeleteClick(client)} color="error">
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

          <TablePagination
            component="div"
            count={totalCount}
            page={page}
            onPageChange={handleChangePage}
            rowsPerPage={rowsPerPage}
            onRowsPerPageChange={handleChangeRowsPerPage}
            rowsPerPageOptions={[5, 10, 25, 50]}
          />

          {/* Create/Edit Dialog */}
          <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
            <DialogTitle>
              {editingClient ? 'Edit Client' : 'Add New Client'}
            </DialogTitle>
            <DialogContent>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
                <TextField
                  label="Name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  error={!!formErrors.name}
                  helperText={formErrors.name}
                  fullWidth
                  required
                />
                <TextField
                  label="Code"
                  value={formData.code}
                  onChange={(e) => setFormData({ ...formData, code: e.target.value })}
                  error={!!formErrors.code}
                  helperText={formErrors.code}
                  fullWidth
                  required
                />
                <TextField
                  label="Description"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  multiline
                  rows={3}
                  fullWidth
                />
                <FormControl fullWidth>
                  <InputLabel required>Status</InputLabel>
                  <Select
                    value={formData.status}
                    label="Status"
                    onChange={(e) => setFormData({ ...formData, status: e.target.value as Client['status'] })}
                    error={!!formErrors.status}
                  >
                    <MenuItem value="ACTIVE">Active</MenuItem>
                    <MenuItem value="INACTIVE">Inactive</MenuItem>
                    <MenuItem value="SUSPENDED">Suspended</MenuItem>
                  </Select>
                  {formErrors.status && (
                    <Typography color="error" variant="caption">
                      {formErrors.status}
                    </Typography>
                  )}
                </FormControl>
              </Box>
            </DialogContent>
            <DialogActions>
              <Button onClick={handleCloseDialog}>Cancel</Button>
              <Button onClick={handleSubmit} variant="contained">
                {editingClient ? 'Update' : 'Create'}
              </Button>
            </DialogActions>
          </Dialog>

          {/* Delete Confirmation Dialog */}
          <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
            <DialogTitle>Confirm Delete</DialogTitle>
            <DialogContent>
              <Typography>
                Are you sure you want to delete the client "{clientToDelete?.name}"?
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

export default ClientManagementPage; 