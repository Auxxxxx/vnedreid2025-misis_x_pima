import "./Classification.css";

const Classification = (props) => {
  const { data } = props;
  return (
    <dl>
      <dt className="dt__category">категория машины:</dt>
      <dd>5, отличный выбор!</dd>
      <dt>
        <span className="dt__title">#степень повреждения в процентах</span>
      </dt>
      <dd>5</dd>
      <dt>
        <span className="dt__title">#тип повреждения</span>
      </dt>
      <dd>#царапины</dd>
    </dl>
  );
};

export default Classification;
