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
      <dd>
        <span className="dd__span">5</span>
      </dd>
      <dt>
        <span className="dt__title">#тип повреждения</span>
      </dt>
      <dd className="dd__list">
        <span className="dd__span">#царапины</span>
        <span className="dd__span">#вмятины</span>
        <span className="dd__span">#семенлохобъелсяблох</span>
        <span className="dd__span">#дырка в попе</span>
      </dd>
    </dl>
  );
};

export default Classification;
