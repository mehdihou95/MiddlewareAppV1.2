import React, { useState, useEffect } from 'react';
import {
  Grid,
  Typography,
  Box,
  Paper,
  List,
  ListItem,
  ListItemText,
  Button,
  Alert,
} from '@mui/material';
import { useDropzone } from 'react-dropzone';
import { TreeView, TreeItem } from '@mui/x-tree-view';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import axios from 'axios';

const TransformationPage = () => {
  const [schema, setSchema] = useState(null);
  const [mappings, setMappings] = useState([]);
  const [selectedElement, setSelectedElement] = useState(null);
  const [selectedField, setSelectedField] = useState(null);
  const [processingStatus, setProcessingStatus] = useState(null);
  const [error, setError] = useState(null);

  const { getRootProps, getInputProps } = useDropzone({
    accept: {
      'application/xml': ['.xsd'],
    },
    onDrop: async (acceptedFiles) => {
      try {
        const formData = new FormData();
        formData.append('file', acceptedFiles[0]);
        await axios.post('/api/schemas', formData);
        setSchema(acceptedFiles[0]);
        setError(null);
      } catch (err) {
        setError('Failed to upload schema: ' + err.message);
      }
    },
  });

  useEffect(() => {
    const fetchMappings = async () => {
      try {
        const response = await axios.get('/api/mappings');
        setMappings(response.data);
      } catch (err) {
        setError('Failed to fetch mappings: ' + err.message);
      }
    };

    const fetchStatus = async () => {
      try {
        const response = await axios.get('/api/process-status');
        setProcessingStatus(response.data);
      } catch (err) {
        setError('Failed to fetch status: ' + err.message);
      }
    };

    fetchMappings();
    fetchStatus();
    const interval = setInterval(fetchStatus, 5000);
    return () => clearInterval(interval);
  }, []);

  const handleCreateMapping = async () => {
    if (!selectedElement || !selectedField) return;

    try {
      await axios.post('/api/mappings', {
        xmlPath: selectedElement,
        dbField: selectedField,
        dataType: 'string', // This should be determined based on the schema
        mandatory: true,
      });
      
      const response = await axios.get('/api/mappings');
      setMappings(response.data);
      setError(null);
    } catch (err) {
      setError('Failed to create mapping: ' + err.message);
    }
  };

  const dbFields = [
    'header.asnNumber',
    'header.shipmentDate',
    'header.supplierId',
    'line.itemNumber',
    'line.quantity',
    'line.uom',
  ];

  return (
    <Grid container spacing={3}>
      <Grid item xs={12}>
        <Typography variant="h4" gutterBottom>
          XML Transformation
        </Typography>
      </Grid>

      {error && (
        <Grid item xs={12}>
          <Alert severity="error">{error}</Alert>
        </Grid>
      )}

      <Grid item xs={12}>
        <Paper
          {...getRootProps()}
          sx={{
            p: 3,
            textAlign: 'center',
            cursor: 'pointer',
            bgcolor: 'grey.100',
          }}
        >
          <input {...getInputProps()} />
          <Typography>
            {schema
              ? `Schema uploaded: ${schema.name}`
              : 'Drag and drop XSD schema here, or click to select'}
          </Typography>
        </Paper>
      </Grid>

      <Grid item xs={6}>
        <Typography variant="h6" gutterBottom>
          XML Schema Elements
        </Typography>
        <Paper sx={{ p: 2, height: 400, overflow: 'auto' }}>
          <TreeView
            defaultCollapseIcon={<ExpandMoreIcon />}
            defaultExpandIcon={<ChevronRightIcon />}
          >
            <TreeItem
              nodeId="1"
              label="ASN"
              onClick={() => setSelectedElement('/ASN')}
            >
              <TreeItem
                nodeId="2"
                label="Header"
                onClick={() => setSelectedElement('/ASN/Header')}
              >
                <TreeItem
                  nodeId="3"
                  label="ASNNumber"
                  onClick={() => setSelectedElement('/ASN/Header/ASNNumber')}
                />
                <TreeItem
                  nodeId="4"
                  label="ShipmentDate"
                  onClick={() => setSelectedElement('/ASN/Header/ShipmentDate')}
                />
                <TreeItem
                  nodeId="5"
                  label="SupplierID"
                  onClick={() => setSelectedElement('/ASN/Header/SupplierID')}
                />
              </TreeItem>
              <TreeItem
                nodeId="6"
                label="Lines"
                onClick={() => setSelectedElement('/ASN/Lines')}
              >
                <TreeItem
                  nodeId="7"
                  label="Line"
                  onClick={() => setSelectedElement('/ASN/Lines/Line')}
                >
                  <TreeItem
                    nodeId="8"
                    label="ItemNumber"
                    onClick={() => setSelectedElement('/ASN/Lines/Line/ItemNumber')}
                  />
                  <TreeItem
                    nodeId="9"
                    label="Quantity"
                    onClick={() => setSelectedElement('/ASN/Lines/Line/Quantity')}
                  />
                  <TreeItem
                    nodeId="10"
                    label="UOM"
                    onClick={() => setSelectedElement('/ASN/Lines/Line/UOM')}
                  />
                </TreeItem>
              </TreeItem>
            </TreeItem>
          </TreeView>
        </Paper>
      </Grid>

      <Grid item xs={6}>
        <Typography variant="h6" gutterBottom>
          Database Fields
        </Typography>
        <Paper sx={{ p: 2, height: 400, overflow: 'auto' }}>
          <List>
            {dbFields.map((field) => (
              <ListItem
                key={field}
                button
                selected={selectedField === field}
                onClick={() => setSelectedField(field)}
              >
                <ListItemText primary={field} />
              </ListItem>
            ))}
          </List>
        </Paper>
      </Grid>

      <Grid item xs={12}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 2 }}>
          <Button
            variant="contained"
            color="primary"
            onClick={handleCreateMapping}
            disabled={!selectedElement || !selectedField}
          >
            Create Mapping
          </Button>
        </Box>
      </Grid>

      <Grid item xs={12}>
        <Typography variant="h6" gutterBottom>
          Current Mappings
        </Typography>
        <List>
          {mappings.map((mapping) => (
            <ListItem key={mapping.id}>
              <ListItemText
                primary={`${mapping.xmlPath} → ${mapping.dbField}`}
                secondary={`Type: ${mapping.dataType}, Mandatory: ${mapping.mandatory}`}
              />
            </ListItem>
          ))}
        </List>
      </Grid>

      {processingStatus && (
        <Grid item xs={12}>
          <Typography variant="h6" gutterBottom>
            Processing Status
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={3}>
              <Paper sx={{ p: 2, textAlign: 'center' }}>
                <Typography variant="h6">{processingStatus.pendingFiles}</Typography>
                <Typography variant="body2">Pending Files</Typography>
              </Paper>
            </Grid>
            <Grid item xs={3}>
              <Paper sx={{ p: 2, textAlign: 'center' }}>
                <Typography variant="h6">{processingStatus.processedFiles}</Typography>
                <Typography variant="body2">Processed Files</Typography>
              </Paper>
            </Grid>
            <Grid item xs={3}>
              <Paper sx={{ p: 2, textAlign: 'center' }}>
                <Typography variant="h6">{processingStatus.failedFiles}</Typography>
                <Typography variant="body2">Failed Files</Typography>
              </Paper>
            </Grid>
            <Grid item xs={3}>
              <Paper sx={{ p: 2, textAlign: 'center' }}>
                <Typography variant="h6">{processingStatus.totalFiles}</Typography>
                <Typography variant="body2">Total Files</Typography>
              </Paper>
            </Grid>
          </Grid>
        </Grid>
      )}
    </Grid>
  );
};

export default TransformationPage; 