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
import { ClientDialog, ConfirmDialog } from '../components';
import { debounce } from 'lodash';
import { handleValidationErrors, handleApiError } from '../utils/errorHandler';
import { PaginationParams, getDefaultPaginationParams } from '../utils/paginationUtils';

type Order = 'asc' | 'desc';

export const ClientManagementPage: React.FC = () => {
  const { 
    clients, 
    loading, 
    error, 
    refreshClients, 
    hasRole, 
    setError,
    isAuthenticated,
    setClients
  } = useClientInterface();
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
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [clientToDelete, setClientToDelete] = useState<Client | null>(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalCount, setTotalCount] = useState<number>(0);
  const [orderBy, setOrderBy] = useState<keyof Client>('name');
  const [order, setOrder] = useState<Order>('asc');
  const [filter, setFilter] = useState('');

  // Memoize and debounce the loadClients function
  const debouncedLoadClients = React.useMemo(
    () =>
      debounce(async () => {
        if (!isAuthenticated) return;
        
        try {
          const params: PaginationParams = {
            page,
            size: rowsPerPage,
            sort: orderBy,
            direction: order,
            filter
          };
          const response = await clientService.getAllClients(params);
          setTotalCount(response.totalElements || 0);
          setClients(response.content);
          setError(null); // Clear any existing error message after successful load
        } catch (err: any) {
          const errorMessage = err.response?.data?.message || err.message || 'Failed to load clients';
          setError(errorMessage);
        }
      }, 300),
    [page, rowsPerPage, orderBy, order, filter, setError, isAuthenticated]
  );

  // Use the debounced function in useEffect
  useEffect(() => {
    debouncedLoadClients();
    // Cleanup
    return () => {
      debouncedLoadClients.cancel();
    };
  }, [debouncedLoadClients]);

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
    console.log('ClientManagementPage: handleSubmit called');
    console.log('ClientManagementPage: Current form data:', formData);
    
    if (!validateForm()) {
      console.log('ClientManagementPage: Form validation failed', formErrors);
      return;
    }

    try {
      const clientData = {
        ...formData,
        code: formData.code.trim().toUpperCase(),
        createdAt: formData.createdAt || new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      console.log('ClientManagementPage: Sending client data to backend:', clientData);

      let savedClient;
      if (editingClient) {
        console.log('ClientManagementPage: Updating existing client');
        savedClient = await clientService.updateClient(editingClient.id, clientData);
      } else {
        console.log('ClientManagementPage: Creating new client');
        savedClient = await clientService.createClient(clientData as Omit<Client, 'id'>);
        console.log('ClientManagementPage: Server response:', savedClient);
      }

      console.log('ClientManagementPage: Client saved successfully');
      handleCloseDialog();
      
      // Force immediate refresh of clients list
      console.log('ClientManagementPage: Forcing immediate refresh of clients list');
      try {
        const params: PaginationParams = {
          page,
          size: rowsPerPage,
          sort: orderBy,
          direction: order,
          filter
        };
        const response = await clientService.getAllClients(params);
        setTotalCount(response.totalElements || 0);
        setClients(response.content);
        setError(null);
        console.log('ClientManagementPage: Clients list refreshed successfully');
      } catch (refreshError: any) {
        console.error('ClientManagementPage: Error refreshing clients:', refreshError);
        const errorMessage = refreshError.response?.data?.message || refreshError.message || 'Failed to refresh clients';
        setError(errorMessage);
      }
    } catch (err: any) {
      console.error('ClientManagementPage: Error saving client:', err);
      console.error('ClientManagementPage: Error details:', {
        response: err.response?.data,
        status: err.response?.status,
        headers: err.response?.headers
      });
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
    setDeleteConfirmOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!clientToDelete) return;

    try {
      await clientService.deleteClient(clientToDelete.id);
      setDeleteConfirmOpen(false);
      setClientToDelete(null);
      await refreshClients();
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to delete client';
      setError(errorMessage);
    }
  };

  const handleDeleteCancel = () => {
    setDeleteConfirmOpen(false);
    setClientToDelete(null);
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
        {hasRole('ADMIN') && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            Add Client
          </Button>
        )}
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
          {error.includes('permission') && !hasRole('ADMIN') && (
            <Typography variant="body2" sx={{ mt: 1 }}>
              You need administrator privileges to manage clients.
            </Typography>
          )}
        </Alert>
      )}

      <TextField
        fullWidth
        label="Filter"
        value={filter}
        onChange={(e) => setFilter(e.target.value)}
        sx={{ mb: 2 }}
      />

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
                        {hasRole('ADMIN') && (
                          <>
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
                          </>
                        )}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>

          <TablePagination
            component="div"
            count={totalCount || 0}
            page={page}
            onPageChange={handleChangePage}
            rowsPerPage={rowsPerPage}
            onRowsPerPageChange={handleChangeRowsPerPage}
            labelDisplayedRows={({ from, to, count }) => 
              `${from}–${to} of ${count !== -1 ? count : 'more than ' + to}`
            }
          />

          <ClientDialog
            open={openDialog}
            onClose={handleCloseDialog}
            onSubmit={handleSubmit}
            client={editingClient}
          />

          <ConfirmDialog
            open={deleteConfirmOpen}
            onClose={handleDeleteCancel}
            onConfirm={handleDeleteConfirm}
            title="Delete Client"
            content={`Are you sure you want to delete ${clientToDelete?.name}?`}
          />
        </>
      )}
    </Box>
  );
};

export default ClientManagementPage; 