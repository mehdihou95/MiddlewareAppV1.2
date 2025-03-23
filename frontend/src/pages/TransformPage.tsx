import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  List,
  ListItem,
  ListItemText,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Snackbar,
  Alert,
  CircularProgress,
} from '@mui/material';
import axios from 'axios';
import RefreshIcon from '@mui/icons-material/Refresh';
import { useClientInterface } from '../context/ClientInterfaceContext';
import ClientInterfaceSelector from '../components/ClientInterfaceSelector';
import { Interface, XsdElement } from '../types';

interface MappingRule {
  id?: number;
  clientId: number;
  interfaceId: number;
  xmlPath: string;
  databaseField: string;
  xsdElement: string;
  tableName: string;
  dataType: string;
  isAttribute: boolean;
  description: string;
}

interface SnackbarState {
  open: boolean;
  message: string;
  severity: 'success' | 'error' | 'info' | 'warning';
}

interface DatabaseField {
  field: string;
  type: string;
}

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const TransformPage: React.FC = () => {
  const { selectedClient, selectedInterface } = useClientInterface();
  const [xsdElements, setXsdElements] = useState<XsdElement[]>([]);
  const [dbFields, setDbFields] = useState<DatabaseField[]>([]);
  const [selectedXsdElement, setSelectedXsdElement] = useState<XsdElement | null>(null);
  const [selectedDbField, setSelectedDbField] = useState('');
  const [mappingRules, setMappingRules] = useState<MappingRule[]>([]);
  const [openDialog, setOpenDialog] = useState(false);
  const [newMapping, setNewMapping] = useState<MappingRule>({
    clientId: 0,
    interfaceId: 0,
    xmlPath: '',
    databaseField: '',
    xsdElement: '',
    tableName: '',
    dataType: '',
    isAttribute: false,
    description: ''
  });
  const [snackbar, setSnackbar] = useState<SnackbarState>({
    open: false,
    message: '',
    severity: 'info'
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (selectedClient && selectedInterface) {
      loadXsdStructure();
      loadMappingRules();
      loadDatabaseFields();
    }
  }, [selectedClient, selectedInterface]);

  const loadXsdStructure = async () => {
    try {
      setLoading(true);
      const response = await axios.get<XsdElement[]>(`${API_URL}/xsd-structure/${selectedInterface?.id}`);
      setXsdElements(response.data);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to load XSD structure';
      setSnackbar({
        open: true,
        message,
        severity: 'error'
      });
    } finally {
      setLoading(false);
    }
  };

  const loadDatabaseFields = async () => {
    if (!selectedClient || !selectedInterface) return;

    try {
      setLoading(true);
      const response = await axios.get<DatabaseField[]>(`${API_URL}/mapping/database-fields`, {
        params: { 
          clientId: selectedClient.id,
          interfaceId: selectedInterface.id
        }
      });
      setDbFields(response.data as DatabaseField[]);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to load database fields';
      setSnackbar({
        open: true,
        message,
        severity: 'error'
      });
    } finally {
      setLoading(false);
    }
  };

  const loadMappingRules = async () => {
    if (!selectedClient || !selectedInterface) return;

    try {
      setLoading(true);
      const response = await axios.get<MappingRule[]>(`${API_URL}/mapping/rules`, {
        params: { 
          clientId: selectedClient.id,
          interfaceId: selectedInterface.id
        }
      });
      setMappingRules(response.data);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to load mapping rules';
      setSnackbar({
        open: true,
        message,
        severity: 'error'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleXsdElementClick = (element: XsdElement) => {
    setSelectedXsdElement(element);
  };

  const handleDbFieldClick = (field: string) => {
    setSelectedDbField(field);
    if (selectedXsdElement && selectedClient && selectedInterface) {
      const isAttribute = selectedXsdElement.path.includes('@');
      const [tableName, fieldName] = field.split('.');
      const dbField = dbFields.find(f => f.field === field);
      
      setNewMapping({
        clientId: selectedClient.id,
        interfaceId: selectedInterface.id,
        xmlPath: selectedXsdElement.path,
        databaseField: fieldName,
        xsdElement: selectedXsdElement.name,
        tableName: tableName,
        dataType: dbField?.type || 'String',
        isAttribute: isAttribute,
        description: `Map ${selectedXsdElement.name} to ${fieldName}`
      });
      setOpenDialog(true);
    }
  };

  const handleSaveMapping = async () => {
    if (!selectedClient || !selectedInterface) {
      setSnackbar({
        open: true,
        message: 'Please select a client and interface first',
        severity: 'error'
      });
      return;
    }

    try {
      if (!newMapping.xmlPath || !newMapping.databaseField) {
        setSnackbar({
          open: true,
          message: 'Please select both XML element and database field',
          severity: 'error'
        });
        return;
      }

      const response = await axios.post<MappingRule>(`${API_URL}/mapping/rules`, newMapping);
      setMappingRules([...mappingRules, response.data]);
      setOpenDialog(false);
      setSelectedXsdElement(null);
      setSelectedDbField('');
      setSnackbar({
        open: true,
        message: 'Mapping rule saved successfully',
        severity: 'success'
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to save mapping rule';
      setSnackbar({
        open: true,
        message,
        severity: 'error'
      });
    }
  };

  const handleDeleteMapping = async (id: number) => {
    try {
      await axios.delete(`${API_URL}/mapping/rules/${id}`);
      setMappingRules(mappingRules.filter(rule => rule.id !== id));
      setSnackbar({
        open: true,
        message: 'Mapping rule deleted successfully',
        severity: 'success'
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to delete mapping rule';
      setSnackbar({
        open: true,
        message,
        severity: 'error'
      });
    }
  };

  const handleSaveAllMappings = async () => {
    if (!selectedClient || !selectedInterface) {
      setSnackbar({
        open: true,
        message: 'Please select a client and interface first',
        severity: 'error'
      });
      return;
    }

    try {
      await axios.post(`${API_URL}/mapping/save-configuration`, {
        clientId: selectedClient.id,
        interfaceId: selectedInterface.id,
        mappings: mappingRules
      });
      setSnackbar({
        open: true,
        message: 'Mapping configuration saved successfully',
        severity: 'success'
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to save mapping configuration';
      setSnackbar({
        open: true,
        message,
        severity: 'error'
      });
    }
  };

  const handleRefreshXsd = async () => {
    try {
      setSnackbar({
        open: true,
        message: 'Refreshing XSD structure...',
        severity: 'info'
      });
      await loadXsdStructure();
      setSnackbar({
        open: true,
        message: 'XSD structure refreshed successfully',
        severity: 'success'
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to refresh XSD structure';
      setSnackbar({
        open: true,
        message,
        severity: 'error'
      });
    }
  };

  const renderMappingRule = (rule: MappingRule) => {
    return (
      <div key={rule.id} className="mapping-rule">
        <Typography>
          {rule.xmlPath} → {rule.tableName}.{rule.databaseField}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {rule.description || 'No description'}
        </Typography>
        <Button
          color="error"
          onClick={() => handleDeleteMapping(rule.id!)}
          sx={{ float: 'right' }}
        >
          DELETE
        </Button>
      </div>
    );
  };

  const handleCloseSnackbar = () => {
    setSnackbar(prev => ({ ...prev, open: false }));
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', p: 3, gap: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4">XML to Database Mapping</Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            color="primary"
            onClick={handleRefreshXsd}
            startIcon={<RefreshIcon />}
            disabled={!selectedClient || !selectedInterface}
          >
            Refresh XSD
          </Button>
          <Button
            variant="contained"
            color="primary"
            onClick={handleSaveAllMappings}
            disabled={!selectedClient || !selectedInterface || mappingRules.length === 0}
          >
            Save Configuration
          </Button>
        </Box>
      </Box>

      <ClientInterfaceSelector required />

      {!selectedClient || !selectedInterface ? (
        <Alert severity="info">
          Please select a client and interface to view and manage mapping rules
        </Alert>
      ) : loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
          <CircularProgress />
        </Box>
      ) : (
        <>
          <Box sx={{ display: 'flex', gap: 2 }}>
            {/* XML Elements Panel */}
            <Paper sx={{ flex: 1, p: 2, maxHeight: '400px', overflow: 'auto' }}>
              <Typography variant="h6" gutterBottom>XML Elements</Typography>
              <List>
                {xsdElements.length === 0 ? (
                  <ListItem>
                    <ListItemText primary="No XML elements available" />
                  </ListItem>
                ) : (
                  xsdElements.map((element, index) => (
                    <ListItem
                      key={index}
                      sx={{ cursor: 'pointer' }}
                      selected={selectedXsdElement?.path === element.path}
                      onClick={() => handleXsdElementClick(element)}
                    >
                      <ListItemText 
                        primary={element.name}
                        secondary={element.path}
                      />
                    </ListItem>
                  ))
                )}
              </List>
            </Paper>

            {/* Database Fields Panel */}
            <Paper sx={{ flex: 1, p: 2, maxHeight: '400px', overflow: 'auto' }}>
              <Typography variant="h6" gutterBottom>Database Fields</Typography>
              <List>
                {dbFields.length === 0 ? (
                  <ListItem>
                    <ListItemText primary="No database fields available" />
                  </ListItem>
                ) : (
                  dbFields.map((field, index) => (
                    <ListItem
                      key={index}
                      sx={{ cursor: 'pointer' }}
                      selected={selectedDbField === field.field}
                      onClick={() => handleDbFieldClick(field.field)}
                    >
                      <ListItemText 
                        primary={field.field}
                        secondary={`Type: ${field.type}`}
                      />
                    </ListItem>
                  ))
                )}
              </List>
            </Paper>
          </Box>

          {/* Mapping Rules Table */}
          <Paper sx={{ p: 2, mt: 2 }}>
            <Typography variant="h6" gutterBottom>Mapping Rules</Typography>
            <List>
              {mappingRules.length === 0 ? (
                <ListItem>
                  <ListItemText primary="No mapping rules defined yet" />
                </ListItem>
              ) : (
                mappingRules.map((rule: MappingRule) => renderMappingRule(rule))
              )}
            </List>
          </Paper>
        </>
      )}

      {/* Mapping Dialog */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)}>
        <DialogTitle>Create Mapping Rule</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            margin="normal"
            label="XML Path"
            value={newMapping.xmlPath}
            disabled
          />
          <TextField
            fullWidth
            margin="normal"
            label="Database Field"
            value={newMapping.databaseField}
            disabled
          />
          <TextField
            fullWidth
            margin="normal"
            label="Description"
            value={newMapping.description}
            onChange={(e) => setNewMapping({...newMapping, description: e.target.value})}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
          <Button onClick={handleSaveMapping} color="primary">Save</Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar for notifications */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={handleCloseSnackbar}
      >
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default TransformPage;