import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, Navigate } from 'react-router-dom';
import Home from './pages/Home';
import Dishes from './pages/Dishes';
import Orders from './pages/Orders';
import Profile from './pages/Profile';
import Login from './pages/Login';
import Register from './pages/Register';
import Admin from './pages/Admin';
import CompanyLogin from './pages/CompanyLogin';
import CompanyDashboard from './pages/CompanyDashboard';
import Navbar from './components/Navbar';
import Cart from './components/Cart';
import SellerDashboard from './pages/SellerDashboard';
import AdminDashboard from './pages/AdminDashboard';
import './App.css';

function App() {
    const isAuthenticated = () => {
        return localStorage.getItem('token') !== null;
    };

    const getUserRole = () => {
        return localStorage.getItem('userRole');
    };

    const PrivateRoute = ({ children, roles }) => {
        if (!isAuthenticated()) {
            return <Navigate to="/login" />;
        }

        if (roles && !roles.includes(getUserRole())) {
            return <Navigate to="/" />;
        }

        return children;
    };

    return (
        <Router>
            <div className="app">
                <Navbar />
                <main className="main-content">
                    <Routes>
                        <Route path="/" element={<Home />} />
                        <Route path="/dishes" element={<Dishes />} />
                        <Route path="/orders" element={<Orders />} />
                        <Route path="/profile" element={<Profile />} />
                        <Route path="/login" element={<Login />} />
                        <Route path="/register" element={<Register />} />
                        <Route path="/admin" element={<Admin />} />
                        <Route path="/company/login" element={<CompanyLogin />} />
                        <Route path="/company/dashboard" element={<CompanyDashboard />} />
                        <Route 
                            path="/cart" 
                            element={
                                <PrivateRoute roles={['CUSTOMER']}>
                                    <Cart />
                                </PrivateRoute>
                            } 
                        />
                        <Route 
                            path="/seller-dashboard" 
                            element={
                                <PrivateRoute roles={['SELLER']}>
                                    <SellerDashboard />
                                </PrivateRoute>
                            } 
                        />
                        <Route 
                            path="/admin-dashboard" 
                            element={
                                <PrivateRoute roles={['ADMIN']}>
                                    <AdminDashboard />
                                </PrivateRoute>
                            } 
                        />
                    </Routes>
                </main>
            </div>
        </Router>
    );
}

export default App; 