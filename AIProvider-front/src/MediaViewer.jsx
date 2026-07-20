import { useEffect, useRef, useState } from "react";
import { ArrowClockwise, ArrowCounterClockwise, ArrowsLeftRight, CaretLeft, CaretRight, MagnifyingGlassMinus, MagnifyingGlassPlus, X } from "@phosphor-icons/react";
import { TransformComponent, TransformWrapper } from "react-zoom-pan-pinch";
import "./MediaViewer.css";

const ZOOM_STEP = 0.2;

export default function MediaViewer({
  title,
  subtitle,
  index,
  total,
  src,
  poster,
  isVideo = false,
  alt = "预览媒体",
  onClose,
  onNavigate,
  onContextMenu,
  actions,
  footer,
}) {
  const transform = useRef(null);
  const [orientation, setOrientation] = useState({ rotation: 0, mirrored: false });

  useEffect(() => {
    setOrientation({ rotation: 0, mirrored: false });
    transform.current?.resetTransform(0, "easeOut");
  }, [src]);

  return <div className="media-viewer" role="dialog" aria-modal="true" aria-label={`预览 ${title || "媒体"}`} onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
    <div className="media-viewer-panel">
      <header className="media-viewer-header">
        <div className="media-viewer-tools">
          {!isVideo && <>
            <button type="button" aria-label="缩小图片" title="缩小" onClick={() => transform.current?.zoomOut(ZOOM_STEP, 120, "easeOut")}><MagnifyingGlassMinus /></button>
            <button type="button" aria-label="恢复原始大小和位置" title="恢复原位" onClick={() => transform.current?.resetTransform(140, "easeOut")}><ArrowCounterClockwise /></button>
            <button type="button" aria-label="放大图片" title="放大" onClick={() => transform.current?.zoomIn(ZOOM_STEP, 120, "easeOut")}><MagnifyingGlassPlus /></button>
            <button type="button" aria-label="顺时针旋转图片 90 度" title="顺时针旋转 90°" onClick={() => setOrientation((current) => ({ ...current, rotation: (current.rotation + 90) % 360 }))}><ArrowClockwise /></button>
            <button type="button" aria-pressed={orientation.mirrored} aria-label="水平镜像图片" title="水平镜像" onClick={() => setOrientation((current) => ({ ...current, mirrored: !current.mirrored }))}><ArrowsLeftRight /></button>
          </>}
        </div>
        <div className="media-viewer-title" title={title}>
          <strong>{title || "媒体"}</strong><span>{index + 1} / {total}</span>{subtitle && <small>{subtitle}</small>}
        </div>
        <div className="media-viewer-actions">{actions}<button type="button" aria-label="关闭预览" title="关闭预览" onClick={onClose}><X /></button></div>
      </header>
      <div className="media-viewer-stage" onContextMenu={onContextMenu}>
        {isVideo ? <video className="media-viewer-video" src={src} poster={poster || undefined} controls autoPlay loop preload="metadata" /> : <TransformWrapper
          ref={transform}
          initialScale={1}
          minScale={1}
          maxScale={8}
          centerOnInit
          centerZoomedOut
          limitToBounds
          disablePadding
          smooth={false}
          wheel={{ step: ZOOM_STEP }}
          doubleClick={{ mode: "toggle", step: 1, animationTime: 180, animationType: "easeOut" }}
          zoomAnimation={{ animationTime: 160, animationType: "easeOut" }}
          autoAlignment={{ animationTime: 140, velocityAlignmentTime: 220, animationType: "easeOut" }}
          panning={{ velocityDisabled: false }}
        >
          <TransformComponent wrapperClass="media-viewer-zoom" contentClass="media-viewer-zoom-content">
            <img className={orientation.rotation % 180 ? "is-quarter-turn" : ""} style={{ transform: `rotate(${orientation.rotation}deg) scaleX(${orientation.mirrored ? -1 : 1})` }} src={src} alt={alt} draggable={false} />
          </TransformComponent>
        </TransformWrapper>}
        {total > 1 && <><button type="button" className="media-viewer-nav prev" aria-label="上一张图片" onClick={() => onNavigate(-1)}><CaretLeft /></button><button type="button" className="media-viewer-nav next" aria-label="下一张图片" onClick={() => onNavigate(1)}><CaretRight /></button></>}
      </div>
      {footer && <footer className="media-viewer-footer">{footer}</footer>}
    </div>
  </div>;
}
