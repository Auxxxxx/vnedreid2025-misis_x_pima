import { useState } from "react";
import "./App.css";
import InputBox from "./components/InputBox/InputBox";
import Gallery from "./components/Gallery/Gallery";

function App() {
  const [galleryImages, setGalleryImages] = useState([]);

  const handleImagesUploaded = (newImages) => {
    // Заменяем все изображения новым (а не добавляем)
    setGalleryImages(newImages);
  };

  const handleRemoveImage = (index) => {
    URL.revokeObjectURL(galleryImages[index]);
    setGalleryImages([]); // Полностью очищаем галерею
  };

  return (
    <div>
      <header className="header">
        <InputBox
          onImagesUploaded={handleImagesUploaded}
          hasImage={galleryImages.length > 0}
        />
      </header>
      <main className="content">
        <Gallery images={galleryImages} onRemoveImage={handleRemoveImage} />
      </main>
    </div>
  );
}

export default App;
