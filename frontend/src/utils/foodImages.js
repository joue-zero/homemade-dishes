// Array of real food images from Unsplash
export const foodImages = [
  'https://images.unsplash.com/photo-1504674900247-0877df9cc836?q=80&w=1000&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80&w=1000&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=1000&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1473093295043-cdd812d0e601?q=80&w=1000&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1476224203421-9ac39bcb3327?q=80&w=1000&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1455619452474-d2be8b1e70cd?q=80&w=1000&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1432139509613-5c4255815697?q=80&w=1000&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1481931098730-318b6f776db0?q=80&w=1000&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1563379926898-05f4575a45d8?q=80&w=1000&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1497888329096-51c27beff665?q=80&w=1000&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1542010589005-d1eacc3918f2?q=80&w=1000&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1567620832903-9fc6debc209f?q=80&w=1000&auto=format&fit=crop'
];

// Function to get a random image from the array
export const getRandomFoodImage = () => {
  const randomIndex = Math.floor(Math.random() * foodImages.length);
  return foodImages[randomIndex];
}; 