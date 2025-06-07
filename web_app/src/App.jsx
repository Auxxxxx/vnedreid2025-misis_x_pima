import { useState } from "react";
import "./App.css";
import InputBox from "./components/InputBox/InputBox";
import Gallery from "./components/Gallery/Gallery";
import AnalyticsChart from "./components/AnalyticsChart/AnalyticsChart";

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
        <section className="information">
          <h1 className="visually-hidden">MISIS x PIMA x AVITO</h1>
          <h2 className="information__title">#классификация</h2>
          <div className="information__graphics">
            <AnalyticsChart></AnalyticsChart>
          </div>
        </section>
        <section className="media">
          <Gallery images={galleryImages} onRemoveImage={handleRemoveImage} />
          <div className="visualuzation">ЗДЕСЬ БУДЕТ 3Д ПИМА</div>
        </section>
      </main>
    </div>
  );
}

export default App;
