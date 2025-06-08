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
  const [uploadedImage, setUploadedImage] = useState(null); // Добавляем состояние для загруженного изображения

  const videoRef = useRef(null);

  const handleImagesUploaded = (newImages) => {
    if (newImages.length === 0) return;
    if (newImages.length === undefined) return;

    const file = newImages[0];
    console.log(file);
    setIsProcessing(true);
    setAnalysisResult(null); // сбрасываем старый результат

    // Сохраняем загруженное изображение для отображения в галерее
    const imageUrl = URL.createObjectURL(file);
    setUploadedImage(imageUrl);
    setGalleryImages([imageUrl]);

    // проигрываем гифку загрузки
    if (videoRef.current) {
      videoRef.current.currentTime = 0;
      videoRef.current.play();
    }

    sendImageToBackend(file);
  };

  const sendImageToBackend = async (file) => {
    const formData = new FormData();
    formData.append("file", file); // Изменяем имя поля на "image" согласно API

    try {
      const response = await fetch(
        "http://109.73.196.162/prediction/api/car-damage/analyze", // Изменяем URL на новый endpoint
        {
          method: "POST",
          body: formData,
          headers: {
            Origin: "*",
          },
        }
      );

      if (!response.ok) {
        throw new Error("Ошибка при отправке изображения");
      }

      const result = await response.text();

      // Преобразуем ответ бэкенда в нужный формат
      const formattedResult = {
        category: result, // Простой ответ от бэкенда
        percentage: 0, // Можно добавить расчет или оставить 0
        res_photo: uploadedImage, // Используем загруженное изображение
        res_gif: null, // GIF не используется в новом API
      };

      setAnalysisResult(formattedResult);
    } catch (error) {
      console.error("Ошибка:", error);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleRemoveImage = (index) => {
    URL.revokeObjectURL(galleryImages[index]);
    setGalleryImages([]);
    setUploadedImage(null);
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
          <h2 className="information__title">#результаты</h2>
          <div className="information__text">
            {analysisResult && (
              <div className="analysis">
                <dl>
                  <dt>
                    <span className="dt__title">состояние</span>
                  </dt>
                  <dd>
                    <span className="dd__span">{analysisResult.category}</span>
                  </dd>
                </dl>
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
                {galleryImages.length === 0
                  ? "как только машина будет в гараже — сразу же покажем!"
                  : "читаем анализ ^_^"}
              </div>
            )}
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;
