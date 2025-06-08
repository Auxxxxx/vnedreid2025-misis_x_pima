import { useState, useRef } from "react";
import "./App.css";
import InputBox from "./components/InputBox/InputBox";
import Gallery from "./components/Gallery/Gallery";
import Classification from "./components/Classification/Classification";
import loadingGif from "./assets/images/0001-0030.webm";

function App() {
  const [galleryImages, setGalleryImages] = useState([]);
  const [isProcessing, setIsProcessing] = useState(false);
  const [analysisResult, setAnalysisResult] = useState(null);

  const videoRef = useRef(null);

  const handleImagesUploaded = (newImages) => {
    if (newImages.length === 0) return;
    if (newImages.length === undefined) return;

    const file = newImages[0];
    console.log(file);
    setIsProcessing(true);
    setAnalysisResult(null); // сбрасываем старый результат

    // проигрываем гифку загрузки
    if (videoRef.current) {
      videoRef.current.currentTime = 0;
      videoRef.current.play();
    }

    sendImageToBackend(file);
  };

  const sendImageToBackend = async (file) => {
    const formData = new FormData();
    formData.append("file", file);
    const file_size = file.size;
    console.log(`Файл доставлен, размер ${file_size}`);

    try {
      const response = await fetch(
        "http://77.95.201.174:8080/prediction/api/submit-photo",
        {
          method: "POST",

          body: formData, // всё, браузер сам всё добавит

          headers: {
            Origin: "*",
          },
        }
      );

      console.log(response);
      formData.forEach((value, key) => {
        console.log("key %s: value %s", key, value);
      });

      if (!response.ok) {
        throw new Error("Ошибка при отправке изображения");
      }

      const result = await response.json();

      // если backend возвращает только путь, допиши хост
      const fullPhotoUrl = result.res_photo.startsWith("http")
        ? result.res_photo
        : `http://109.73.196.162${result.res_photo}`;
      const fullGifUrl = result.res_gif.startsWith("http")
        ? result.res_gif
        : `http://109.73.196.162${result.res_gif}`;

      setGalleryImages([fullPhotoUrl]);
      setAnalysisResult({
        ...result,
        res_photo: fullPhotoUrl,
        res_gif: fullGifUrl,
      });
    } catch (error) {
      console.error("Ошибка:", error);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleRemoveImage = (index) => {
    URL.revokeObjectURL(galleryImages[index]);
    setGalleryImages([]);
    setIsProcessing(false);
    setAnalysisResult(null);
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
            {analysisResult && (
              <div className="analysis">
                <h3>Результаты анализа:</h3>
                <ul>
                  <li>Категория: {analysisResult.category}</li>
                  <li>Вмятина: {analysisResult.dent ? "Да" : "Нет"}</li>
                  <li>Царапина: {analysisResult.scratch ? "Да" : "Нет"}</li>
                  <li>Ржавчина: {analysisResult.rust ? "Да" : "Нет"}</li>
                  <li>
                    Деформация: {analysisResult.deformation ? "Да" : "Нет"}
                  </li>
                  <li>Степень повреждения: {analysisResult.percentage}%</li>
                </ul>
                {analysisResult.res_gif && (
                  <video
                    src={analysisResult.res_gif}
                    autoPlay
                    loop
                    muted
                    style={{ maxWidth: "100%", marginTop: "1em" }}
                  />
                )}
              </div>
            )}
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
