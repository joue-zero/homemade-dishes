import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './SellerDashboard.css';

const SellerDashboard = () => {
    const [activeTab, setActiveTab] = useState('dishes');
    const [dishes, setDishes] = useState([]);
    const [orders, setOrders] = useState([]);
    const [soldDishes, setSoldDishes] = useState([]);
    const [newDish, setNewDish] = useState({
        name: '',
        description: '',
        price: '',
        category: '',
        imageUrl: '',
        quantity: '',
        available: true
    });
    const [editingDish, setEditingDish] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState({
        dishes: true,
        orders: true,
        soldDishes: true
    });

    // Shipping companies for the sold dishes view
    const shippingCompanies = [
        'Fast Delivery Inc.',
        'Express Shipping Co.',
        'Quick & Safe Logistics',
        'Reliable Delivery Services',
        'Swift Transport Solutions'
    ];

    // Sample data for fallbacks (kept for graceful fallback)
    const sampleOrders = [
        {
            id: 1,
            customerName: 'John Doe',
            createdAt: new Date().toISOString(),
            status: 'PENDING',
            items: [
                { id: 1, dishName: 'Sample Dish 1', quantity: 2, price: 12.99 },
                { id: 2, dishName: 'Sample Dish 2', quantity: 1, price: 8.99 }
            ],
            totalAmount: 34.97
        }
    ];

    const sampleSoldDishes = [
        {
            dishId: 1,
            orderId: 101,
            dishName: 'Sample Sold Dish',
            orderDate: new Date().toISOString(),
            quantity: 3,
            price: 15.99,
            customerName: 'Jane Smith',
            customerEmail: 'jane@example.com',
            customerPhone: '123-456-7890',
            shippingCompany: 'Fast Delivery Inc.',
            deliveryStatus: 'Delivered',
            shippingAddress: '123 Main St, Anytown, USA'
        }
    ];

    useEffect(() => {
        fetchDishes();
        fetchOrders();
        fetchSoldDishes();
    }, []);

    const fetchDishes = async () => {
        setLoading(prev => ({ ...prev, dishes: true }));
        try {
            const userId = localStorage.getItem('userId') || '1'; // Fallback to ID 1 if not found
            
            // Create headers with or without token
            const headers = {};
            const token = localStorage.getItem('token');
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }
            
            const response = await axios.get(
                `http://localhost:8082/api/sellers/${userId}/dishes`, 
                { headers }
            );
            
            console.log("Fetched dishes:", response.data);
            setDishes(response.data);
            setError(null);
        } catch (error) {
            console.error('Error fetching dishes:', error);
            setError('Failed to load dishes. Using sample data for now.');
            // If we already have dishes data, keep it, otherwise set empty array
            if (dishes.length === 0) {
                setDishes([]);
            }
        } finally {
            setLoading(prev => ({ ...prev, dishes: false }));
        }
    };

    const fetchOrders = async () => {
        setLoading(prev => ({ ...prev, orders: true }));
        try {
            // Try the order service directly (port 8083)
            const userId = localStorage.getItem('userId');
            try {
                const response = await axios.get(`http://localhost:8083/api/orders/seller/${userId}`, {
                    headers: { 
                        'Authorization': `Bearer ${localStorage.getItem('token')}`,
                        'X-User-Id': userId
                    },
                    timeout: 5000
                });
                setOrders(response.data);
                setError(null);
            } catch (primaryError) {
                console.error('Error fetching orders from primary endpoint:', primaryError);
                
                // Try through gateway (port 8081) if direct call fails
                const response = await axios.get(`http://localhost:8081/api/orders/seller/${userId}`, {
                    headers: { 
                        'Authorization': `Bearer ${localStorage.getItem('token')}`,
                        'X-User-Id': userId
                    },
                    timeout: 5000
                });
                setOrders(response.data);
                setError(null);
            }
        } catch (error) {
            console.error('Error fetching orders:', error);
            setError('Failed to load orders. Using sample data for now.');
            setOrders(sampleOrders);
        } finally {
            setLoading(prev => ({ ...prev, orders: false }));
        }
    };

    const fetchSoldDishes = async () => {
        setLoading(prev => ({ ...prev, soldDishes: true }));
        try {
            const userId = localStorage.getItem('userId');
            
            // Get completed orders from the API
            let completedOrders = [];
            try {
                const response = await axios.get(`http://localhost:8083/api/orders/seller/${userId}`, {
                    headers: { 
                        'Authorization': `Bearer ${localStorage.getItem('token')}`,
                        'X-User-Id': userId
                    },
                    timeout: 5000
                });
                // Filter for completed orders
                completedOrders = response.data.filter(order => order.status === 'COMPLETED');
            } catch (primaryError) {
                console.error('Error fetching completed orders from primary endpoint:', primaryError);
                
                // Try gateway if direct call fails
                const response = await axios.get(`http://localhost:8081/api/orders/seller/${userId}`, {
                    headers: { 
                        'Authorization': `Bearer ${localStorage.getItem('token')}`,
                        'X-User-Id': userId
                    },
                    timeout: 5000
                });
                // Filter for completed orders
                completedOrders = response.data.filter(order => order.status === 'COMPLETED');
            }
            
            // Transform orders into sold dishes format
            const soldDishesData = completedOrders.flatMap(order => {
                return order.items.map(item => ({
                    dishId: item.dishId,
                    orderId: order.id,
                    dishName: item.dishName,
                    orderDate: order.createdAt,
                    quantity: item.quantity,
                    price: item.price,
                    customerName: order.customerName || 'Customer',
                    customerEmail: order.customerEmail || 'customer@example.com',
                    customerPhone: order.customerPhone || '123-456-7890',
                    shippingCompany: shippingCompanies[Math.floor(Math.random() * shippingCompanies.length)],
                    deliveryStatus: 'Delivered',
                    shippingAddress: order.shippingAddress || '123 Main St, Anytown, USA'
                }));
            });
            
            setSoldDishes(soldDishesData);
            setError(null);
        } catch (error) {
            console.error('Error processing sold dishes:', error);
            setError('Failed to load sales history. Using sample data for now.');
            setSoldDishes(sampleSoldDishes);
        } finally {
            setLoading(prev => ({ ...prev, soldDishes: false }));
        }
    };

    const handleCreateDish = async (e) => {
        e.preventDefault();
        try {
            const userId = localStorage.getItem('userId') || '1'; // Fallback to ID 1 if not found
            
            // Format the dish data correctly
            const formattedDish = {
                ...newDish,
                price: parseFloat(newDish.price),
                quantity: newDish.quantity ? parseInt(newDish.quantity) : null
            };
            
            console.log("Creating dish:", formattedDish);
            
            // Create headers with or without token
            const headers = {};
            const token = localStorage.getItem('token');
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }
            
            const response = await axios.post(
                `http://localhost:8082/api/sellers/${userId}/dishes`,
                formattedDish,
                { headers }
            );
            
            console.log("Create dish response:", response.data);
            
            setDishes([...dishes, response.data]);
            setNewDish({
                name: '',
                description: '',
                price: '',
                category: '',
                imageUrl: '',
                quantity: '',
                available: true
            });
            setError(null);
        } catch (error) {
            console.error('Error creating dish:', error);
            setError('Failed to create dish. Please try again.');
        }
    };

    const handleEditDish = (dish) => {
        const dishToEdit = {
            ...dish,
            price: dish.price.toString(),
            quantity: dish.quantity ? dish.quantity.toString() : ''
        };
        console.log("Setting dish for editing:", dishToEdit);
        console.log("Original quantity:", dish.quantity, "Converted quantity:", dishToEdit.quantity);
        console.log("Original category:", dish.category);
        setEditingDish(dishToEdit);
    };

    // Add a useEffect to monitor the editingDish state
    useEffect(() => {
        if (editingDish) {
            console.log("Current editingDish state:", editingDish);
        }
    }, [editingDish]);

    const handleUpdateDish = async (e) => {
        e.preventDefault();
        try {
            const userId = localStorage.getItem('userId') || '1'; // Fallback to ID 1 if not found
            
            // Log the dish being updated
            console.log("Updating dish raw form state:", editingDish);
            
            // Convert price and quantity to appropriate formats and ensure all fields are properly formatted
            const updatedDish = {
                id: editingDish.id,
                name: editingDish.name,
                description: editingDish.description,
                price: parseFloat(editingDish.price),
                category: editingDish.category,
                imageUrl: editingDish.imageUrl,
                available: editingDish.available,
                quantity: editingDish.quantity ? parseInt(editingDish.quantity) : null,
                sellerId: parseInt(userId)
            };
            
            console.log("Formatted dish data:", updatedDish);
            console.log("Specifically checking category:", updatedDish.category);
            console.log("Specifically checking quantity:", updatedDish.quantity);
            
            // Create headers with or without token
            const headers = {};
            const token = localStorage.getItem('token');
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }
            
            const response = await axios.put(
                `http://localhost:8082/api/sellers/${userId}/dishes/${updatedDish.id}`,
                updatedDish,
                { headers }
            );
            
            console.log("Update response:", response.data);
            console.log("Response category:", response.data.category);
            console.log("Response quantity:", response.data.quantity);
            
            // Update the dishes state with the updated dish
            setDishes(dishes.map(dish => 
                dish.id === updatedDish.id ? response.data : dish
            ));
            
            setEditingDish(null);
            setError(null);
        } catch (error) {
            console.error('Error updating dish:', error);
            setError('Failed to update dish. Please try again.');
        }
    };

    const handleCancelEdit = () => {
        setEditingDish(null);
    };

    const handleToggleAvailability = async (dishId, currentStatus) => {
        try {
            const userId = localStorage.getItem('userId') || '1'; // Fallback to ID 1 if not found
            
            // Create headers with or without token
            const headers = {};
            const token = localStorage.getItem('token');
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }
            
            const response = await axios.put(
                `http://localhost:8082/api/sellers/${userId}/dishes/${dishId}/availability?available=${!currentStatus}`,
                null, // No body needed as it's in the query parameter
                { headers }
            );
            
            console.log("Toggle availability response:", response.data);
            
            setDishes(dishes.map(dish => 
                dish.id === dishId ? response.data : dish
            ));
            setError(null);
        } catch (error) {
            console.error('Error updating dish availability:', error);
            setError('Failed to update availability. Please try again.');
        }
    };

    const handleUpdateOrderStatus = async (orderId, newStatus) => {
        try {
            const userId = localStorage.getItem('userId');
            console.log(`Attempting to update order ${orderId} to status ${newStatus}`);
            
            // Try direct service first
            try {
                const response = await axios.put(
                    `http://localhost:8083/api/orders/${orderId}/status`, 
                    { status: String(newStatus) }, // Ensure it's a string
                    { 
                        headers: { 
                            'Authorization': `Bearer ${localStorage.getItem('token')}`,
                            'X-User-Id': userId
                        },
                        timeout: 5000
                    }
                );
                console.log('Status update response:', response.data);
            } catch (primaryError) {
                console.error('Error with primary endpoint:', primaryError.response?.data || primaryError.message);
                
                // Log the request payload for debugging
                console.log('Request payload for fallback:', { status: String(newStatus) });
                
                // Fallback to gateway
                const response = await axios.put(
                    `http://localhost:8083/api/orders/${orderId}/status`, 
                    { status: String(newStatus) }, // Ensure it's a string
                    { 
                        headers: { 
                            'Authorization': `Bearer ${localStorage.getItem('token')}`,
                            'X-User-Id': userId
                        },
                        timeout: 5000
                    }
                );
                console.log('Fallback status update response:', response.data);
            }
            
            // Update local state
            setOrders(orders.map(order => 
                order.id === orderId ? { ...order, status: newStatus } : order
            ));
            
            // If the status is COMPLETED, refresh the sales history
            if (newStatus === 'COMPLETED') {
                fetchSoldDishes();
            }
            
            setError(null);
        } catch (error) {
            console.error('Error updating order status:', error.response?.data || error.message);
            setError(`Failed to update order status: ${error.response?.data || error.message}`);
        }
    };

    return (
        <div className="seller-dashboard">
            <h1>Seller Dashboard</h1>
            
            {error && <div className="error-message">{error}</div>}
            
            <div className="dashboard-tabs">
                <button 
                    className={`tab ${activeTab === 'dishes' ? 'active' : ''}`}
                    onClick={() => setActiveTab('dishes')}
                >
                    Manage Dishes
                </button>
                <button 
                    className={`tab ${activeTab === 'orders' ? 'active' : ''}`}
                    onClick={() => setActiveTab('orders')}
                >
                    Current Orders
                </button>
                <button 
                    className={`tab ${activeTab === 'sold' ? 'active' : ''}`}
                    onClick={() => setActiveTab('sold')}
                >
                    Sales History
                </button>
            </div>
            
            {activeTab === 'dishes' && (
                <>
                    <section className="create-dish">
                        <h2>{editingDish ? 'Update Dish' : 'Add New Dish'}</h2>
                        <form onSubmit={editingDish ? handleUpdateDish : handleCreateDish}>
                            <input
                                type="text"
                                placeholder="Dish Name"
                                value={editingDish ? editingDish.name : newDish.name}
                                onChange={(e) => editingDish 
                                    ? setEditingDish({...editingDish, name: e.target.value})
                                    : setNewDish({...newDish, name: e.target.value})
                                }
                                required
                            />
                            <textarea
                                placeholder="Description"
                                value={editingDish ? editingDish.description : newDish.description}
                                onChange={(e) => editingDish 
                                    ? setEditingDish({...editingDish, description: e.target.value})
                                    : setNewDish({...newDish, description: e.target.value})
                                }
                                required
                            />
                            <div className="form-row">
                                <input
                                    type="number"
                                    placeholder="Price"
                                    value={editingDish ? editingDish.price : newDish.price}
                                    onChange={(e) => editingDish 
                                        ? setEditingDish({...editingDish, price: e.target.value})
                                        : setNewDish({...newDish, price: e.target.value})
                                    }
                                    required
                                />
                                <input
                                    type="number"
                                    placeholder="Quantity Available"
                                    value={editingDish ? editingDish.quantity : newDish.quantity}
                                    onChange={(e) => editingDish 
                                        ? setEditingDish({...editingDish, quantity: e.target.value})
                                        : setNewDish({...newDish, quantity: e.target.value})
                                    }
                                    required
                                />
                            </div>
                            <input
                                type="text"
                                placeholder="Category"
                                value={editingDish ? editingDish.category : newDish.category}
                                onChange={(e) => editingDish 
                                    ? setEditingDish({...editingDish, category: e.target.value})
                                    : setNewDish({...newDish, category: e.target.value})
                                }
                                required
                            />
                            <input
                                type="text"
                                placeholder="Image URL"
                                value={editingDish ? editingDish.imageUrl : newDish.imageUrl}
                                onChange={(e) => editingDish 
                                    ? setEditingDish({...editingDish, imageUrl: e.target.value})
                                    : setNewDish({...newDish, imageUrl: e.target.value})
                                }
                            />
                            <div className="form-buttons">
                                <button type="submit" className="primary-btn">
                                    {editingDish ? 'Update Dish' : 'Add Dish'}
                                </button>
                                {editingDish && (
                                    <button 
                                        type="button" 
                                        className="cancel-btn"
                                        onClick={handleCancelEdit}
                                    >
                                        Cancel
                                    </button>
                                )}
                            </div>
                        </form>
                    </section>

                    <section className="dishes-section">
                        <h2>Your Dishes</h2>
                        {loading.dishes ? (
                            <div className="loading">Loading dishes...</div>
                        ) : dishes.length === 0 ? (
                            <p className="empty-message">You haven't added any dishes yet.</p>
                        ) : (
                            <div className="dishes-grid">
                                {dishes.map(dish => (
                                    <div key={dish.id} className="dish-card">
                                        {dish.imageUrl && <img src={dish.imageUrl} alt={dish.name} />}
                                        <h3>{dish.name}</h3>
                                        <p>{dish.description}</p>
                                        <p>Price: ${dish.price}</p>
                                        <p>Category: {dish.category}</p>
                                        {dish.quantity && <p>Available: {dish.quantity} items</p>}
                                        <div className="dish-card-actions">
                                            <button 
                                                onClick={() => handleToggleAvailability(dish.id, dish.available)}
                                                className={dish.available ? 'available' : 'unavailable'}
                                            >
                                                {dish.available ? 'Available' : 'Unavailable'}
                                            </button>
                                            <button 
                                                onClick={() => handleEditDish(dish)}
                                                className="edit-btn"
                                            >
                                                Edit
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </section>
                </>
            )}

            {activeTab === 'orders' && (
                <section className="orders-section">
                    <h2>Current Orders</h2>
                    {loading.orders ? (
                        <div className="loading">Loading orders...</div>
                    ) : orders.length === 0 ? (
                        <p className="empty-message">No current orders</p>
                    ) : (
                        <div className="orders-list">
                            {orders.map(order => (
                                <div key={order.id} className="order-card">
                                    <h3>Order #{order.id}</h3>
                                    <p>Customer: {order.customerName}</p>
                                    <p>Date: {new Date(order.createdAt).toLocaleString()}</p>
                                    <p>Status: {order.status}</p>
                                    <div className="order-items">
                                        {order.items.map(item => (
                                            <div key={item.id} className="order-item">
                                                <p>{item.dishName} x {item.quantity}</p>
                                                <p>${item.price * item.quantity}</p>
                                            </div>
                                        ))}
                                    </div>
                                    <div className="order-total">
                                        <p>Total: ${order.totalAmount}</p>
                                    </div>
                                    <div className="order-actions">
                                        {order.status === 'PENDING' && (
                                            <>
                                                <button 
                                                    onClick={() => handleUpdateOrderStatus(order.id, 'ACCEPTED')}
                                                    className="accept"
                                                >
                                                    Accept
                                                </button>
                                                <button 
                                                    onClick={() => handleUpdateOrderStatus(order.id, 'REJECTED')}
                                                    className="reject"
                                                >
                                                    Reject
                                                </button>
                                            </>
                                        )}
                                        {order.status === 'ACCEPTED' && (
                                            <button 
                                                onClick={() => handleUpdateOrderStatus(order.id, 'READY')}
                                                className="ready"
                                            >
                                                Mark as Ready
                                            </button>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </section>
            )}

            {activeTab === 'sold' && (
                <section className="sold-dishes-section">
                    <h2>Sales History</h2>
                    {loading.soldDishes ? (
                        <div className="loading">Loading sales history...</div>
                    ) : soldDishes.length === 0 ? (
                        <p className="empty-message">No sales history available</p>
                    ) : (
                        <div className="sold-dishes-list">
                            {soldDishes.map(item => (
                                <div key={`${item.dishId}-${item.orderId}`} className="sold-dish-card">
                                    <div className="sold-dish-info">
                                        <h3>{item.dishName}</h3>
                                        <p>Order Date: {new Date(item.orderDate).toLocaleDateString()}</p>
                                        <p>Quantity Sold: {item.quantity}</p>
                                        <p>Price per Unit: ${item.price}</p>
                                        <p>Total: ${item.price * item.quantity}</p>
                                    </div>
                                    <div className="customer-shipping-info">
                                        <div className="info-section">
                                            <h4>Customer</h4>
                                            <p>{item.customerName}</p>
                                            <p>{item.customerEmail}</p>
                                            <p>{item.customerPhone}</p>
                                        </div>
                                        <div className="info-section">
                                            <h4>Shipping Details</h4>
                                            <p>Company: {item.shippingCompany}</p>
                                            <p>Status: {item.deliveryStatus || 'Delivered'}</p>
                                            <p>Address: {item.shippingAddress}</p>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </section>
            )}
        </div>
    );
};

export default SellerDashboard; 