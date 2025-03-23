import React, { useState, useEffect } from 'react';
import {
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    IconButton,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TablePagination,
    TableRow,
    TextField,
    Typography,
    FormControlLabel,
    Switch,
    Chip,
    Tooltip,
    Alert,
    Snackbar
} from '@mui/material';
import {
    Add as AddIcon,
    Edit as EditIcon,
    Delete as DeleteIcon,
    Lock as LockIcon,
    LockOpen as LockOpenIcon,
    Refresh as RefreshIcon
} from '@mui/icons-material';
import { userService } from '../services/userService';
import { User } from '../types';
import { handleApiError, isValidationError, getValidationErrors } from '../utils/errorHandler';

interface UserFormData {
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    password?: string;
    roles: string[];
    enabled: boolean;
}

interface UserFormErrors {
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    password?: string;
}

const UserManagement: React.FC = () => {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [openDialog, setOpenDialog] = useState<boolean>(false);
    const [editingUser, setEditingUser] = useState<User | null>(null);
    const [formData, setFormData] = useState<UserFormData>({
        username: '',
        email: '',
        firstName: '',
        lastName: '',
        roles: [],
        enabled: true
    });
    const [formErrors, setFormErrors] = useState<UserFormErrors>({
        username: '',
        email: '',
        firstName: '',
        lastName: ''
    });
    const [page, setPage] = useState<number>(0);
    const [rowsPerPage, setRowsPerPage] = useState<number>(10);
    const [totalElements, setTotalElements] = useState(0);
    const [searchTerm, setSearchTerm] = useState('');
    const [enabledFilter, setEnabledFilter] = useState<boolean | null>(null);
    const [snackbar, setSnackbar] = useState<{
        open: boolean;
        message: string;
        severity: 'success' | 'error' | 'info' | 'warning';
    }>({
        open: false,
        message: '',
        severity: 'info'
    });

    const fetchUsers = async () => {
        try {
            let response;
            if (searchTerm) {
                response = await userService.searchUsers(searchTerm, page, rowsPerPage);
            } else if (enabledFilter !== null) {
                response = await userService.getUsersByStatus(enabledFilter, page, rowsPerPage);
            } else {
                response = await userService.getAllUsers(page, rowsPerPage);
            }
            setUsers(response.content);
            setTotalElements(response.totalElements);
        } catch (error) {
            handleApiError(error, (message) => showSnackbar(message, 'error'));
        }
    };

    useEffect(() => {
        fetchUsers();
    }, [page, rowsPerPage, searchTerm, enabledFilter]);

    const handleChangePage = (event: unknown, newPage: number) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    const handleOpenDialog = (user?: User) => {
        if (user) {
            setEditingUser(user);
            setFormData(user);
        } else {
            setEditingUser(null);
            setFormData({
                username: '',
                email: '',
                firstName: '',
                lastName: '',
                roles: [],
                enabled: true
            });
        }
        setOpenDialog(true);
    };

    const handleCloseDialog = () => {
        setOpenDialog(false);
        setEditingUser(null);
        setFormData({
            username: '',
            email: '',
            firstName: '',
            lastName: '',
            roles: [],
            enabled: true
        });
    };

    const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value, checked } = event.target;
        setFormData(prev => ({
            ...prev,
            [name]: name === 'enabled' ? checked : value
        }));
    };

    const handleSubmit = async () => {
        try {
            if (editingUser) {
                await userService.updateUser(editingUser.id, formData);
                showSnackbar('User updated successfully', 'success');
            } else {
                await userService.createUser(formData as Omit<User, 'id'>);
                showSnackbar('User created successfully', 'success');
            }
            handleCloseDialog();
            fetchUsers();
        } catch (error) {
            if (isValidationError(error)) {
                const validationErrors = getValidationErrors(error);
                validationErrors.forEach(error => showSnackbar(error, 'error'));
            } else {
                handleApiError(error, (message) => showSnackbar(message, 'error'));
            }
        }
    };

    const handleDelete = async (id: number) => {
        if (window.confirm('Are you sure you want to delete this user?')) {
            try {
                await userService.deleteUser(id);
                showSnackbar('User deleted successfully', 'success');
                fetchUsers();
            } catch (error) {
                handleApiError(error, (message) => showSnackbar(message, 'error'));
            }
        }
    };

    const handleUnlockAccount = async (id: number) => {
        try {
            await userService.unlockAccount(id);
            showSnackbar('Account unlocked successfully', 'success');
            fetchUsers();
        } catch (error) {
            handleApiError(error, (message) => showSnackbar(message, 'error'));
        }
    };

    const showSnackbar = (message: string, severity: 'success' | 'error') => {
        setSnackbar({ open: true, message, severity });
    };

    const handleCloseSnackbar = () => {
        setSnackbar(prev => ({ ...prev, open: false }));
    };

    return (
        <Box sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
                <Typography variant="h4">User Management</Typography>
                <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={() => handleOpenDialog()}
                >
                    Add User
                </Button>
            </Box>

            <Box sx={{ mb: 3, display: 'flex', gap: 2 }}>
                <TextField
                    label="Search Users"
                    variant="outlined"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    sx={{ flexGrow: 1 }}
                />
                <FormControlLabel
                    control={
                        <Switch
                            checked={enabledFilter === true}
                            onChange={(e) => setEnabledFilter(e.target.checked ? true : null)}
                        />
                    }
                    label="Show Enabled Only"
                />
                <Button
                    variant="outlined"
                    startIcon={<RefreshIcon />}
                    onClick={() => {
                        setSearchTerm('');
                        setEnabledFilter(null);
                        setPage(0);
                    }}
                >
                    Reset Filters
                </Button>
            </Box>

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Username</TableCell>
                            <TableCell>Email</TableCell>
                            <TableCell>Name</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell>Account Status</TableCell>
                            <TableCell>Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {users.map((user) => (
                            <TableRow key={user.id}>
                                <TableCell>{user.username}</TableCell>
                                <TableCell>{user.email}</TableCell>
                                <TableCell>{`${user.firstName} ${user.lastName}`}</TableCell>
                                <TableCell>
                                    <Chip
                                        label={user.enabled ? 'Enabled' : 'Disabled'}
                                        color={user.enabled ? 'success' : 'error'}
                                    />
                                </TableCell>
                                <TableCell>
                                    {user.accountLocked ? (
                                        <Tooltip title="Account is locked">
                                            <LockIcon color="error" />
                                        </Tooltip>
                                    ) : (
                                        <Tooltip title="Account is active">
                                            <LockOpenIcon color="success" />
                                        </Tooltip>
                                    )}
                                </TableCell>
                                <TableCell>
                                    <IconButton onClick={() => handleOpenDialog(user)}>
                                        <EditIcon />
                                    </IconButton>
                                    <IconButton onClick={() => handleDelete(user.id)}>
                                        <DeleteIcon />
                                    </IconButton>
                                    {user.accountLocked && (
                                        <IconButton onClick={() => handleUnlockAccount(user.id)}>
                                            <LockOpenIcon />
                                        </IconButton>
                                    )}
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            <TablePagination
                component="div"
                count={totalElements}
                page={page}
                onPageChange={handleChangePage}
                rowsPerPage={rowsPerPage}
                onRowsPerPageChange={handleChangeRowsPerPage}
                rowsPerPageOptions={[5, 10, 25]}
            />

            <Dialog open={openDialog} onClose={handleCloseDialog}>
                <DialogTitle>{editingUser ? 'Edit User' : 'Add User'}</DialogTitle>
                <DialogContent>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
                        <TextField
                            label="Username"
                            name="username"
                            value={formData.username || ''}
                            onChange={handleInputChange}
                            fullWidth
                        />
                        <TextField
                            label="Email"
                            name="email"
                            type="email"
                            value={formData.email || ''}
                            onChange={handleInputChange}
                            fullWidth
                        />
                        <TextField
                            label="First Name"
                            name="firstName"
                            value={formData.firstName || ''}
                            onChange={handleInputChange}
                            fullWidth
                        />
                        <TextField
                            label="Last Name"
                            name="lastName"
                            value={formData.lastName || ''}
                            onChange={handleInputChange}
                            fullWidth
                        />
                        {!editingUser && (
                            <TextField
                                label="Password"
                                name="password"
                                type="password"
                                value={formData.password || ''}
                                onChange={handleInputChange}
                                fullWidth
                            />
                        )}
                        <FormControlLabel
                            control={
                                <Switch
                                    name="enabled"
                                    checked={formData.enabled || false}
                                    onChange={handleInputChange}
                                />
                            }
                            label="Enabled"
                        />
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseDialog}>Cancel</Button>
                    <Button onClick={handleSubmit} variant="contained">
                        {editingUser ? 'Update' : 'Create'}
                    </Button>
                </DialogActions>
            </Dialog>

            <Snackbar
                open={snackbar.open}
                autoHideDuration={6000}
                onClose={handleCloseSnackbar}
            >
                <Alert
                    onClose={handleCloseSnackbar}
                    severity={snackbar.severity}
                    sx={{ width: '100%' }}
                >
                    {snackbar.message}
                </Alert>
            </Snackbar>
        </Box>
    );
};

export default UserManagement; 