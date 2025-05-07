import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Home from './pages/Home';
import Dishes from './pages/Dishes';
import Orders from './pages/Orders';
import Profile from './pages/Profile';
import Login from './pages/Login';
import Register from './pages/Register';
import Admin from './pages/Admin';
import CompanyLogin from './pages/CompanyLogin';
import CompanyDashboard from './pages/CompanyDashboard';
import './App.css';

function App() {
    return (
        <Router>
            <div className="app">
                <nav>
                    <ul>
                        <li><Link to="/">Home</Link></li>
                        <li><Link to="/dishes">Dishes</Link></li>
                        <li><Link to="/orders">Orders</Link></li>
                        <li><Link to="/profile">Profile</Link></li>
                        <li><Link to="/login">Login</Link></li>
                        <li><Link to="/register">Register</Link></li>
                        <li><Link to="/admin">Admin</Link></li>
                        <li><Link to="/company/login">Company Login</Link></li>
                    </ul>
                </nav>

                <main>
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
                    </Routes>
                </main>
            </div>
        </Router>
    );
}

export default App; 