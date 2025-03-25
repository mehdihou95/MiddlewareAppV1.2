import React, { useState, useEffect } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Box, FormControl, InputLabel, Select, MenuItem, Typography } from '@mui/material';
import { Client } from '../types';

interface ClientDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (formData: Omit<Client, 'id'>) => Promise<void>;
  client: Client | null;
}

interface FormErrors {
  name?: string;
  code?: string;
  status?: string;
}

export const ClientDialog: React.FC<ClientDialogProps> = ({ open, onClose, onSubmit, client }) => {
  const [formData, setFormData] = useState<Omit<Client, 'id'>>({
    name: '',
    code: '',
    description: '',
    status: 'ACTIVE',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  });

  const [formErrors, setFormErrors] = useState<FormErrors>({});

  useEffect(() => {
    if (client) {
      const { id, ...clientData } = client;
      setFormData(clientData);
    } else {
      setFormData({
        name: '',
        code: '',
        description: '',
        status: 'ACTIVE',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      });
    }
    setFormErrors({});
  }, [client]);

  const validateForm = (): boolean => {
    const errors: FormErrors = {};
    if (!formData.name.trim()) {
      errors.name = 'Name is required';
    }
    if (!formData.code.trim()) {
      errors.code = 'Code is required';
    }
    if (!formData.status) {
      errors.status = 'Status is required';
    }
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async () => {
    if (!validateForm()) return;
    await onSubmit(formData);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        {client ? 'Edit Client' : 'Add New Client'}
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
        <Button onClick={onClose}>Cancel</Button>
        <Button onClick={handleSubmit} variant="contained">
          {client ? 'Update' : 'Create'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ClientDialog; 