import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './AdminDashboard.css';

const AdminDashboard = () => {
    const [activeTab, setActiveTab] = useState('customers');
    const [customers, setCustomers] = useState([]);
    const [sellers, setSellers] = useState([]);
    const [newSeller, setNewSeller] = useState({
        username: '',
        password: ''
    });
    const [generatedCredentials, setGeneratedCredentials] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchAccounts();
    }, []);

    const fetchAccounts = async () => {
        setError(null);
        try {
            // Get all users and filter by role client-side
            const response = await axios.get('http://localhost:8081/api/users', {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
            });
            
            if (response.data) {
                // Filter users by role
                const allUsers = Array.isArray(response.data) ? response.data : [];
                setCustomers(allUsers.filter(user => user.role === 'CUSTOMER'));
                setSellers(allUsers.filter(user => user.role === 'SELLER'));
            }
        } catch (error) {
            console.error('Error fetching accounts:', error);
            setError('Failed to load accounts. Please try again later.');
            
            // Set empty arrays as fallback
            setCustomers([]);
            setSellers([]);
        }
    };

    const generateCredentials = () => {
        const username = `seller_${Math.random().toString(36).substring(2, 8)}`;
        const length = 12;
        const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*";
        let password = "";
        for (let i = 0; i < length; i++) {
            password += charset.charAt(Math.floor(Math.random() * charset.length));
        }
        setGeneratedCredentials({ username, password });
        setNewSeller(prev => ({ ...prev, username, password }));
    };

    const handleCreateSeller = async (e) => {
        e.preventDefault();
        setError(null);
        try {
            const response = await axios.post(
                'http://localhost:8081/api/users/register',
                {
                    username: newSeller.username,
                    password: newSeller.password,
                    role: 'SELLER'
                }
            );
            
            if (response.data) {
                setSellers([...sellers, response.data]);
                setNewSeller({ username: '', password: '' });
                setGeneratedCredentials(null);
                alert('Seller account created successfully!');
            }
        } catch (error) {
            console.error('Error creating seller account:', error);
            setError('Failed to create seller account. Please try again.');
        }
    };

    const handleDeleteAccount = async (userId, role) => {
        setError(null);
        try {
            // Try using a different endpoint that might be supported
            // Some APIs use POST for delete operations instead of DELETE
            await axios.post(
                `http://localhost:8081/api/users/delete/${userId}`,
                {},
                { headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` } }
            );
            
            if (role === 'CUSTOMER') {
                setCustomers(customers.filter(c => c.id !== userId));
            } else if (role === 'SELLER') {
                setSellers(sellers.filter(s => s.id !== userId));
            }
            
            alert('Account deleted successfully!');
        } catch (error) {
            console.error('Error deleting account:', error);
            setError(`Failed to delete account. Please try again.`);
        }
    };

    return (
        <div className="admin-dashboard">
            <h1>Admin Dashboard</h1>

            {error && <div className="error-message">{error}</div>}

            <div className="tabs">
                <button 
                    className={`tab ${activeTab === 'customers' ? 'active' : ''}`}
                    onClick={() => setActiveTab('customers')}
                >
                    Customer Accounts
                </button>
                <button 
                    className={`tab ${activeTab === 'sellers' ? 'active' : ''}`}
                    onClick={() => setActiveTab('sellers')}
                >
                    Seller Accounts
                </button>
                <button 
                    className={`tab ${activeTab === 'create-seller' ? 'active' : ''}`}
                    onClick={() => setActiveTab('create-seller')}
                >
                    Create Seller Account
                </button>
            </div>

            {activeTab === 'customers' && (
                <section className="admin-section">
                    <h2>Customer Accounts ({customers.length})</h2>
                    {customers.length === 0 ? (
                        <p className="empty-message">No customer accounts found.</p>
                    ) : (
                        <div className="accounts-list">
                            {customers.map(customer => (
                                <div key={customer.id} className="account-card">
                                    <div className="account-info">
                                        <h3>{customer.username}</h3>
                                        {customer.email && <p>Email: {customer.email}</p>}
                                        {customer.createdAt && 
                                            <p>Joined: {new Date(customer.createdAt).toLocaleDateString()}</p>
                                        }
                                    </div>
                                    <div className="account-actions">
                                        <button 
                                            className="delete"
                                            onClick={() => handleDeleteAccount(customer.id, 'CUSTOMER')}
                                        >
                                            Delete
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </section>
            )}

            {activeTab === 'sellers' && (
                <section className="admin-section">
                    <h2>Seller Accounts ({sellers.length})</h2>
                    {sellers.length === 0 ? (
                        <p className="empty-message">No seller accounts found.</p>
                    ) : (
                        <div className="accounts-list">
                            {sellers.map(seller => (
                                <div key={seller.id} className="account-card">
                                    <div className="account-info">
                                        <h3>{seller.username}</h3>
                                        {seller.companyName && <p>Company: {seller.companyName}</p>}
                                        {seller.email && <p>Email: {seller.email}</p>}
                                        {seller.createdAt && 
                                            <p>Joined: {new Date(seller.createdAt).toLocaleDateString()}</p>
                                        }
                                    </div>
                                    <div className="account-actions">
                                        <button 
                                            className="delete"
                                            onClick={() => handleDeleteAccount(seller.id, 'SELLER')}
                                        >
                                            Delete
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </section>
            )}

            {activeTab === 'create-seller' && (
                <section className="admin-section">
                    <h2>Create Seller Account</h2>
                    <form className="create-seller-form" onSubmit={handleCreateSeller}>
                        <div className="credentials-section">
                            <button 
                                type="button" 
                                className="generate-btn"
                                onClick={generateCredentials}
                            >
                                Generate Credentials
                            </button>
                            {generatedCredentials && (
                                <div className="generated-credentials">
                                    <div className="credential-item">
                                        <label>Username:</label>
                                        <span>{generatedCredentials.username}</span>
                                    </div>
                                    <div className="credential-item">
                                        <label>Password:</label>
                                        <span>{generatedCredentials.password}</span>
                                    </div>
                                    <p className="warning">
                                        Please save these credentials. They won't be shown again.
                                    </p>
                                </div>
                            )}
                        </div>
                        <button type="submit" disabled={!generatedCredentials}>
                            Create Seller Account
                        </button>
                    </form>
                </section>
            )}
        </div>
    );
};

export default AdminDashboard; 