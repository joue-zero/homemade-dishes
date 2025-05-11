import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
  const navigate = useNavigate();
  const isAuthenticated = localStorage.getItem('token');
  const userRole = localStorage.getItem('userRole');

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('userRole');
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/">Home-made Dishes</Link>
      </div>
      <div className="navbar-links">
        <Link to="/" className="nav-link">Home</Link>
        <Link to="/dishes" className="nav-link">Dishes</Link>
        {isAuthenticated && (
          <>
            {userRole === 'SELLER' && (
              <Link to="/seller-dashboard" className="nav-link">Seller Dashboard</Link>
            )}
            {userRole === 'ADMIN' && (
              <Link to="/admin-dashboard" className="nav-link">Admin Dashboard</Link>
            )}
            {userRole === 'CUSTOMER' && (
              <Link to="/cart" className="nav-link">Cart</Link>
            )}
            <Link to="/orders" className="nav-link">My Orders</Link>
            <Link to="/profile" className="nav-link">Profile</Link>
          </>
        )}
      </div>
      <div className="navbar-auth">
        {isAuthenticated ? (
          <button onClick={handleLogout} className="nav-button">Logout</button>
        ) : (
          <>
            <Link to="/login" className="nav-button">Login</Link>
            <Link to="/register" className="nav-button register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar; 