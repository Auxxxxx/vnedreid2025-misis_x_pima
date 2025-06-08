import { useEffect } from "react";
import "./Gallery.css";
import placeholderImage from "../../assets/images/waiting.gif";

const Gallery = ({ images = [], onRemoveImage }) => {
  // Очистка URL при размонтировании
  useEffect(() => {
    return () => {
      images.forEach((url) => URL.revokeObjectURL(url));
    };
  }, [images]);

  return (
    <div className="gallery-grid">
      {images.length > 0 ? (
        images.map((img, index) => (
          <div key={index} className="gallery-item">
            <img src={img} alt={`Uploaded ${index}`} />
            <button onClick={() => onRemoveImage(index)} className="remove-btn">
              ×
            </button>
          </div>
        ))
      ) : (
        <div className="gallery-placeholder">
          <img
            src={placeholderImage}
            alt="Ожидаем загрузку изображения"
            className="placeholder-image"
          />
        </div>
      )}
    </div>
  );
};

export default Gallery;
