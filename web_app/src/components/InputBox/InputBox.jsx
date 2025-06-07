import { useEffect, useState } from "react";
import "./InputBox.css";

const InputBox = ({ onImagesUploaded, hasImage }) => {
  const handleFileChange = (e) => {
    const files = Array.from(e.target.files);

    // Ограничиваем загрузку только одним файлом
    if (files.length > 1) {
      alert("Можно загрузить только одно изображение");
      e.target.value = null; // Сбрасываем выбор файлов
      return;
    }

    const imageUrl = URL.createObjectURL(files[0]);
    onImagesUploaded([imageUrl]); // Передаем массив с одним изображением

    e.target.value = null;
  };

  return (
    <div className="image-uploader">
      <input
        type="file"
        id="multi-upload"
        accept="image/*"
        onChange={handleFileChange}
        className="visually-hidden"
      />
      <label htmlFor="multi-upload" className="custom-upload-button">
        <span className="upload-icon"></span>
        <span className="upload-text">
          {hasImage
            ? "(⁠⊙⁠_⁠◎⁠) хотите заменить?"
            : "(⁠ﾉ⁠*⁠0⁠*⁠)⁠ﾉ загрузите изображение"}
        </span>
      </label>
    </div>
  );
};

export default InputBox;
