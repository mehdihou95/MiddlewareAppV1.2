import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../config/apiConfig';
import { tokenManager, TokenResponse } from '../services/tokenManager';

interface AuthContextType {
    isAuthenticated: boolean;
    user: { username: string; roles: string[] } | null;
    loading: boolean;
    error: string | null;
    login: (username: string, password: string) => Promise<void>;
    logout: () => void;
    validateToken: () => Promise<boolean>;
}

interface ValidateResponse {
    valid: boolean;
    username: string;
    roles: string[];
    error?: string;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
    const [user, setUser] = useState<{ username: string; roles: string[] } | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();

    const validateToken = useCallback(async () => {
        try {
            const response = await api.get<ValidateResponse>('/auth/validate');
            const { valid, username, roles } = response.data;
            
            if (valid) {
                setUser({ username, roles });
                setIsAuthenticated(true);
                return true;
            } else {
                tokenManager.clearTokens();
                setUser(null);
                setIsAuthenticated(false);
                return false;
            }
        } catch (err) {
            tokenManager.clearTokens();
            setUser(null);
            setIsAuthenticated(false);
            return false;
        }
    }, []);

    const login = async (username: string, password: string) => {
        try {
            setError(null);
            const response = await api.post<TokenResponse>('/auth/login', {
                username,
                password
            });

            const { token, refreshToken, roles } = response.data;
            tokenManager.setAccessToken(token);
            tokenManager.setRefreshToken(refreshToken);
            setUser({ username, roles });
            setIsAuthenticated(true);
            navigate('/');
        } catch (err: any) {
            const message = err.response?.data?.message || err.message || 'Login failed';
            setError(message);
            throw new Error(message);
        }
    };

    const logout = useCallback(() => {
        tokenManager.clearTokens();
        setUser(null);
        setIsAuthenticated(false);
        navigate('/login');
    }, [navigate]);

    useEffect(() => {
        const initAuth = async () => {
            setLoading(true);
            const token = tokenManager.getAccessToken();
            
            if (token && tokenManager.isTokenValid(token)) {
                const isValid = await validateToken();
                if (!isValid) {
                    navigate('/login');
                }
            } else {
                tokenManager.clearTokens();
                setIsAuthenticated(false);
                setUser(null);
            }
            
            setLoading(false);
        };

        initAuth();
    }, [validateToken, navigate]);

    return (
        <AuthContext.Provider value={{
            isAuthenticated,
            user,
            loading,
            error,
            login,
            logout,
            validateToken
        }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}; 