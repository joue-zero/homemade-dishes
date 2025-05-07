import React from 'react';
import { Link } from 'react-router-dom';
import './Home.css';

const Home = () => {
  return (
    <div className="home">
      <section className="hero">
        <h1>Welcome to Home-made Dishes</h1>
        <p>Discover delicious homemade meals from talented local chefs</p>
        <Link to="/dishes" className="cta-button">Browse Dishes</Link>
      </section>

      <section className="features">
        <div className="feature-card">
          <h3>Fresh & Homemade</h3>
          <p>All dishes are prepared fresh by local chefs in their own kitchens</p>
        </div>
        <div className="feature-card">
          <h3>Local Flavors</h3>
          <p>Experience authentic local cuisine from talented home chefs</p>
        </div>
        <div className="feature-card">
          <h3>Easy Ordering</h3>
          <p>Simple and secure ordering process with real-time updates</p>
        </div>
      </section>

      <section className="how-it-works">
        <h2>How It Works</h2>
        <div className="steps">
          <div className="step">
            <div className="step-number">1</div>
            <h3>Browse Dishes</h3>
            <p>Explore our selection of homemade dishes from local chefs</p>
          </div>
          <div className="step">
            <div className="step-number">2</div>
            <h3>Place Order</h3>
            <p>Select your favorite dishes and place your order</p>
          </div>
          <div className="step">
            <div className="step-number">3</div>
            <h3>Enjoy</h3>
            <p>Receive your delicious homemade meal and enjoy!</p>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Home; 