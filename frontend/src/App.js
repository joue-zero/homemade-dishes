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
import Navbar from './components/Navbar';
import './App.css';

function App() {
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
                    </Routes>
                </main>
            </div>
        </Router>
    );
}

export default App; 