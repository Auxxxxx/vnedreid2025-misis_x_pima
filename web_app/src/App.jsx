import { useState, useRef } from "react";
import "./App.css";
import InputBox from "./components/InputBox/InputBox";
import Gallery from "./components/Gallery/Gallery";
import Classification from "./components/Classification/Classification";
import loadingGif from "./assets/images/0001-0030.webm";

function App() {
  const [galleryImages, setGalleryImages] = useState([]);
  const [isProcessing, setIsProcessing] = useState(false);

  const videoRef = useRef(null);

  const handleImagesUploaded = (newImages) => {
    setGalleryImages(newImages);
    setIsProcessing(true);
    // видео при загрузке изображения
    if (videoRef.current) {
      videoRef.current.currentTime = 0;
      videoRef.current.play();
    }
  };
  const handleRemoveImage = (index) => {
    URL.revokeObjectURL(galleryImages[index]);
    setGalleryImages([]);
    setIsProcessing(false);
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
          <div className="information__text">
            <Classification />
          </div>
        </section>
        <section className="media">
          <Gallery images={galleryImages} onRemoveImage={handleRemoveImage} />

          <div className="visualization">
            {isProcessing ? (
              <div className="processing-indicator">
                <video
                  ref={videoRef}
                  src={loadingGif}
                  autoPlay
                  loop
                  muted
                  className="processing-video"
                >
                  ваш браузер не поддерживает видео...
                </video>
                <p>сейчас проанализируем повреждения...</p>
              </div>
            ) : (
              <div className="placeholder">
                как только машина будет в гараже — сразу же покажем!
              </div>
            )}
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;
