import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './CompanyDashboard.css';

const CompanyDashboard = () => {
    const [dishes, setDishes] = useState([]);
    const [company, setCompany] = useState(null);
    const [newDish, setNewDish] = useState({
        name: '',
        description: '',
        price: '',
        category: '',
        available: true
    });

    useEffect(() => {
        const storedCompany = JSON.parse(localStorage.getItem('company'));
        setCompany(storedCompany);
    }, []);

    useEffect(() => {
        if (company) {
            fetchDishes();
        }
    }, [company]);

    const fetchDishes = async () => {
        try {
            const response = await axios.get(`http://localhost:8082/api/companies/${company.id}/dishes`);
            setDishes(Array.isArray(response.data) ? response.data : []);
        } catch (error) {
            console.error('Error fetching dishes:', error);
            setDishes([]);
        }
    };

    const handleCreateDish = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post(`http://localhost:8082/api/companies/${company.id}/dishes`, newDish);
            setDishes(prevDishes => [...prevDishes, response.data]);
            setNewDish({
                name: '',
                description: '',
                price: '',
                category: '',
                available: true
            });
            // Refresh dishes after adding new one
            fetchDishes();
        } catch (error) {
            console.error('Error creating dish:', error);
            alert('Error creating dish');
        }
    };

    const handleToggleAvailability = async (dishId, currentStatus) => {
        try {
            const response = await axios.put(
                `http://localhost:8082/api/companies/${company.id}/dishes/${dishId}/availability`,
                null,
                { params: { available: !currentStatus } }
            );
            setDishes(prevDishes => prevDishes.map(dish => 
                dish.id === dishId ? response.data : dish
            ));
        } catch (error) {
            console.error('Error updating dish availability:', error);
        }
    };

    if (!company) {
        return <div>Please log in first</div>;
    }

    return (
        <div className="company-dashboard">
            <h1>Company Dashboard</h1>
            <div className="company-info">
                <h2>{company.name}</h2>
                <p>Email: {company.email}</p>
            </div>

            <div className="create-dish">
                <h2>Add New Dish</h2>
                <form onSubmit={handleCreateDish}>
                    <input
                        type="text"
                        placeholder="Dish Name"
                        value={newDish.name}
                        onChange={(e) => setNewDish({...newDish, name: e.target.value})}
                        required
                    />
                    <textarea
                        placeholder="Description"
                        value={newDish.description}
                        onChange={(e) => setNewDish({...newDish, description: e.target.value})}
                        required
                    />
                    <input
                        type="number"
                        placeholder="Price"
                        value={newDish.price}
                        onChange={(e) => setNewDish({...newDish, price: e.target.value})}
                        required
                    />
                    <input
                        type="text"
                        placeholder="Category"
                        value={newDish.category}
                        onChange={(e) => setNewDish({...newDish, category: e.target.value})}
                        required
                    />
                    <button type="submit">Add Dish</button>
                </form>
            </div>

            <div className="dishes-list">
                <h2>Your Dishes</h2>
                <div className="dishes-grid">
                    {Array.isArray(dishes) && dishes.map(dish => (
                        <div key={dish.id} className="dish-card">
                            <h3>{dish.name}</h3>
                            <p>{dish.description}</p>
                            <p>Price: ${dish.price}</p>
                            <p>Category: {dish.category}</p>
                            <button 
                                onClick={() => handleToggleAvailability(dish.id, dish.available)}
                                className={dish.available ? 'available' : 'unavailable'}
                            >
                                {dish.available ? 'Available' : 'Unavailable'}
                            </button>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default CompanyDashboard; 