import { useState } from "react";
import "./App.css";
import InputBox from "./components/InputBox/InputBox";
import Gallery from "./components/Gallery/Gallery";

function App() {
  const [galleryImages, setGalleryImages] = useState([]);

  const handleImagesUploaded = (newImages) => {
    setGalleryImages((prev) => [...prev, ...newImages]);
  };

  const handleRemoveImage = (index) => {
    URL.revokeObjectURL(galleryImages[index]);
    setGalleryImages((prev) => prev.filter((_, i) => i !== index));
  };

  return (
    <div>
      <header className="header">
        <InputBox onImagesUploaded={handleImagesUploaded} />
      </header>
      <main className="content">
        <Gallery images={galleryImages} onRemoveImage={handleRemoveImage} />
      </main>
    </div>
  );
}

export default App;
