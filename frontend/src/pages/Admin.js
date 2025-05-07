import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './Admin.css';

const Admin = () => {
    const [companies, setCompanies] = useState([]);
    const [newCompany, setNewCompany] = useState({
        name: '',
        email: '',
        phone: '',
        address: ''
    });

    useEffect(() => {
        fetchCompanies();
    }, []);

    const fetchCompanies = async () => {
        try {
            const response = await axios.get('http://localhost:8082/api/admin/companies');
            setCompanies(response.data);
        } catch (error) {
            console.error('Error fetching companies:', error);
        }
    };

    const handleCreateCompany = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post('http://localhost:8082/api/admin/companies', newCompany);
            setCompanies([...companies, response.data]);
            setNewCompany({ name: '', email: '', phone: '', address: '' });
            alert(`Company created! Password: ${response.data.password}`);
        } catch (error) {
            console.error('Error creating company:', error);
            alert('Error creating company');
        }
    };

    return (
        <div className="admin-page">
            <h1>Admin Dashboard</h1>
            
            <div className="create-company">
                <h2>Create New Company</h2>
                <form onSubmit={handleCreateCompany}>
                    <input
                        type="text"
                        placeholder="Company Name"
                        value={newCompany.name}
                        onChange={(e) => setNewCompany({...newCompany, name: e.target.value})}
                        required
                    />
                    <input
                        type="email"
                        placeholder="Email"
                        value={newCompany.email}
                        onChange={(e) => setNewCompany({...newCompany, email: e.target.value})}
                        required
                    />
                    <input
                        type="text"
                        placeholder="Phone"
                        value={newCompany.phone}
                        onChange={(e) => setNewCompany({...newCompany, phone: e.target.value})}
                        required
                    />
                    <input
                        type="text"
                        placeholder="Address"
                        value={newCompany.address}
                        onChange={(e) => setNewCompany({...newCompany, address: e.target.value})}
                        required
                    />
                    <button type="submit">Create Company</button>
                </form>
            </div>

            <div className="companies-list">
                <h2>Companies</h2>
                <table>
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Address</th>
                        </tr>
                    </thead>
                    <tbody>
                        {companies.map(company => (
                            <tr key={company.id}>
                                <td>{company.name}</td>
                                <td>{company.email}</td>
                                <td>{company.phone}</td>
                                <td>{company.address}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default Admin; 