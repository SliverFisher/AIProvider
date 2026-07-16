import { ArrowRight, Heart, ImageSquare, MagicWand, Sparkle } from "@phosphor-icons/react";
import { showImages } from "./DynamicShowcase";
import "./CuteWorkshopCard.css";

export function CuteWorkshopArt() {
  const current = showImages[0];
  return <div className="cute-workshop-art" aria-hidden="true">
    {current && <img key={current.path} className="cute-workshop-preview preview-front" src={current.src} alt="" />}
    <i className="cute-card-heart"><Heart weight="fill" /></i>
    <i className="cute-card-spark"><Sparkle weight="fill" /></i>
  </div>;
}

export default function CuteWorkshopCard({ onOpen }) {
  return <button className="cute-workshop-card" onClick={onOpen}>
    <CuteWorkshopArt />
    <span className="cute-workshop-copy">
      <small><MagicWand weight="duotone" /> MAID MAGIC STUDIO</small>
      <strong>进入图像工坊 <b>♡</b></strong>
      <em>把脑海里的二次元灵感，变成今天最可爱的作品</em>
      <span className="cute-workshop-tags"><i><ImageSquare />本机生成</i><i>LoRA 魔法</i><i>资产收藏</i></span>
    </span>
    <span className="cute-workshop-go"><ArrowRight weight="bold" /><small>START</small></span>
  </button>;
}
