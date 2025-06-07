import { useEffect, useState } from "react";
import "./InputBox.css";

const InputBox = ({ onImagesUploaded }) => {
  const [images, setImages] = useState([]);
  const [isUploading, setIsUploading] = useState(false);

  useEffect(() => {
    return () => {
      images.forEach((url) => URL.revokeObjectURL(url));
    };
  }, [images]);

  // Обработка выбора файлов
  const handleFileChange = (e) => {
    const files = Array.from(e.target.files);
    const imageUrls = files.map((file) => URL.createObjectURL(file));
    setImages((prev) => [...prev, ...imageUrls]);

    // Передаём новые изображения в родительский компонент
    onImagesUploaded(imageUrls); // Добавляем эту строку

    e.target.value = null;
  };

  // Отправка на бэкенд
  const uploadToBackend = async () => {
    if (images.length === 0) return;

    setIsUploading(true);
    const formData = new FormData();

    images.forEach((url, index) => {
      fetch(url)
        .then((res) => res.blob())
        .then((blob) => {
          formData.append("images", blob, `image-${index}.jpg`);
        });
    });

    try {
      const response = await fetch("https://your-backend-api/upload", {
        method: "POST",
        body: formData,
      });
      console.log("Успешно загружено:", await response.json());
    } catch (error) {
      console.error("Ошибка:", error);
    } finally {
      setIsUploading(false);
    }
  };

  // Удаление изображения
  const removeImage = (index) => {
    // Освобождаем URL перед удалением
    URL.revokeObjectURL(images[index]);
    setImages((prev) => prev.filter((_, i) => i !== index));
  };

  return (
    <div className="image-uploader">
      <input
        type="file"
        id="multi-upload"
        multiple
        accept="image/*"
        onChange={handleFileChange}
        className="visually-hidden"
      />

      {/* Кастомная кнопка-лейбл */}
      <label htmlFor="multi-upload" className="custom-upload-button">
        <span className="upload-icon">📁</span>
        <span className="upload-text">#загрузите изображения</span>
      </label>
    </div>
  );
};

export default InputBox;
