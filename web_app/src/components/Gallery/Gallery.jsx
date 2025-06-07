import { useEffect } from "react";
import "./Gallery.css";

const Gallery = ({ images = [], onRemoveImage }) => {
  // Очистка URL при размонтировании
  useEffect(() => {
    return () => {
      images.forEach((url) => URL.revokeObjectURL(url));
    };
  }, [images]);

  return (
    <div className="gallery-grid">
      {images.map((img, index) => (
        <div key={index} className="gallery-item">
          <img src={img} alt={`Uploaded ${index}`} />
          <button onClick={() => onRemoveImage(index)} className="remove-btn">
            ×
          </button>
        </div>
      ))}
    </div>
  );
};

export default Gallery;
