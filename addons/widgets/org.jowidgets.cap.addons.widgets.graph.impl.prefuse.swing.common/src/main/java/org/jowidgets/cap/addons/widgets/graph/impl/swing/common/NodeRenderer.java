/*
 * Copyright (c) 2012, sapalm
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * * Neither the name of the jo-widgets.org nor the
 *   names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL jo-widgets.org BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package org.jowidgets.cap.addons.widgets.graph.impl.swing.common;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.RectangularShape;
import java.net.URL;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.ui.api.icons.CapIcons;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.image.IImageHandle;
import org.jowidgets.spi.impl.swing.common.image.SwingImageRegistry;

import prefuse.Constants;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

public class NodeRenderer extends LabelRenderer {

	private GradientPaint gradientColor;
	private final AffineTransform transform;
	private final Image expandedIcon;
	private final Image notExpandedIcon;

	public NodeRenderer(final String name, final String image) {

		super(name, image);
		transform = new AffineTransform();
		expandedIcon = initializeIcons(CapIcons.NODE_CONTRACTED);
		notExpandedIcon = initializeIcons(CapIcons.NODE_EXPANDED);

		this.setHorizontalPadding(20);
		this.setVerticalPadding(2);
		this.setRoundedCorner(8, 8);

	}

	private Image initializeIcons(final IImageConstant iconImage) {
		final IImageConstant icon = iconImage;
		if (icon != null) {
			final IImageHandle imageHandle = Toolkit.getImageRegistry().getImageHandle(icon);
			if (imageHandle != null) {
				final Object image = imageHandle.getImage();
				final URL imageUrl = imageHandle.getImageUrl();
				if (image instanceof Image) {
					return (Image) image;
				}
				else if (imageUrl != null) {
					IImageHandle awtImageHandle = SwingImageRegistry.getInstance().getImageHandle(icon);
					if (awtImageHandle == null) {
						SwingImageRegistry.getInstance().registerImageConstant(icon, imageUrl);
						awtImageHandle = SwingImageRegistry.getInstance().getImageHandle(icon);
					}
					return (Image) awtImageHandle.getImage();
				}
			}
		}
		return null;
	}

	@Override
	protected Shape getRawShape(final VisualItem item) {
		final Shape shape = super.getRawShape(item);

		//		final Shape newShape;

		//		if ((Boolean) item.get("isParent")) {
		//			newShape = new RoundRectangle2D.Double();
		//			((RoundRectangle2D) newShape).setRoundRect(
		//					shape.getBounds().x,
		//					shape.getBounds().y,
		//					shape.getBounds().width,
		//					shape.getBounds().height,
		//					8,
		//					8);
		//
		//		}
		//		else {
		//			newShape = new RoundRectangle2D.Double();
		//			((RoundRectangle2D) newShape).setRoundRect(
		//					shape.getBounds().x,
		//					shape.getBounds().y,
		//					shape.getBounds().width,
		//					shape.getBounds().height,
		//					8,
		//					8);
		//		}
		return shape;
	}

	@Override
	public void render(final Graphics2D g, final VisualItem item) {
		final RectangularShape shape = (RectangularShape) getShape(item);

		gradientColor = new GradientPaint(
			shape.getBounds().x + shape.getBounds().width / 2,
			shape.getBounds().y,
			lightenFillColor(item.getFillColor()),
			shape.getBounds().x + shape.getBounds().width / 2,
			shape.getBounds().y + shape.getBounds().height / 2,
			ColorLib.getColor(item.getFillColor()),
			false);
		g.setPaint(gradientColor);
		g.fill(shape);

		final String text = m_text;
		final Image img = getImage(item);

		if (text == null && img == null) {
			return;
		}

		final double size = item.getSize();
		final boolean useInt = 1.5 > Math.max(g.getTransform().getScaleX(), g.getTransform().getScaleY());
		double x = shape.getMinX() + size * m_horizBorder;
		double y = shape.getMinY() + size * m_vertBorder;

		// render image
		renderImage(g, useInt, img, size, x, y, shape);

		if (img != null) {
			final double w = size * img.getWidth(null);
			final double h = size * img.getHeight(null);
			double ix = x;
			double iy = y;

			// determine one co-ordinate based on the image position
			switch (m_imagePos) {
				case Constants.LEFT:
					x += w + size * m_imageMargin;
					break;
				case Constants.RIGHT:
					ix = shape.getMaxX() - size * m_horizBorder - w;
					break;
				case Constants.TOP:
					y += h + size * m_imageMargin;
					break;
				case Constants.BOTTOM:
					iy = shape.getMaxY() - size * m_vertBorder - h;
					break;
				default:
					throw new IllegalStateException("Unrecognized image alignment setting.");
			}

			// determine the other coordinate based on image alignment
			switch (m_imagePos) {
				case Constants.LEFT:
				case Constants.RIGHT:
					// need to set image y-coordinate
					switch (m_vImageAlign) {
						case Constants.TOP:
							break;
						case Constants.BOTTOM:
							iy = shape.getMaxY() - size * m_vertBorder - h;
							break;
						case Constants.CENTER:
							iy = shape.getCenterY() - h / 2;
							break;
						default:
							break;
					}
					break;
				case Constants.TOP:
				case Constants.BOTTOM:
					// need to set image x-coordinate
					switch (m_hImageAlign) {
						case Constants.LEFT:
							break;
						case Constants.RIGHT:
							ix = shape.getMaxX() - size * m_horizBorder - w;
							break;
						case Constants.CENTER:
							ix = shape.getCenterX() - w / 2;
							break;
						default:
							break;
					}
					break;
				default:
					break;
			}

			if (useInt && size == 1.0) {
				// if possible, use integer precision
				// results in faster, flicker-free image rendering
				g.drawImage(img, (int) ix, (int) iy, null);
			}
			else {
				transform.setTransform(size, 0, 0, size, ix, iy);
				g.drawImage(img, transform, null);
			}
		}

		// render text
		final int textColor = item.getTextColor();
		if (text != null && ColorLib.alpha(textColor) > 0) {
			g.setPaint(ColorLib.getColor(textColor));
			g.setFont(m_font);
			final FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);

			// compute available width
			double tw;
			switch (m_imagePos) {
				case Constants.TOP:
				case Constants.BOTTOM:
					tw = shape.getWidth() - 2 * size * m_horizBorder;
					break;
				default:
					tw = m_textDim.width;
			}

			// compute available height
			double th;
			switch (m_imagePos) {
				case Constants.LEFT:
				case Constants.RIGHT:
					th = shape.getHeight() - 2 * size * m_vertBorder;
					break;
				default:
					th = m_textDim.height;
			}

			// compute starting y-coordinate
			y += fm.getAscent();
			switch (m_vTextAlign) {
				case Constants.TOP:
					break;
				case Constants.BOTTOM:
					y += th - m_textDim.height;
					break;
				case Constants.CENTER:
					y += (th - m_textDim.height) / 2;
					break;
				default:
					break;
			}

			// render each line of text
			final int lh = fm.getHeight(); // the line height
			int start = 0;
			int end = text.indexOf(m_delim);
			for (; end >= 0; y += lh) {
				drawString(g, fm, text.substring(start, end), useInt, x, y, tw);
				start = end + 1;
				end = text.indexOf(m_delim, start);
			}
			drawString(g, fm, text.substring(start), useInt, x, y, tw);
		}

		//render (+) or (-) if isParent and expanded
		if ((Boolean) item.get("isParent") && !(Boolean) item.get("expanded")) {
			renderImage(g, useInt, expandedIcon, size, shape.getX() + 1, shape.getY(), shape);
		}
		else if ((Boolean) item.get("isParent") && (Boolean) item.get("expanded")) {
			renderImage(g, useInt, notExpandedIcon, size, shape.getX() + 1, shape.getY(), shape);
		}
	}

	private Color lightenFillColor(final int fillColor) {
		final float[] hsv = Color.RGBtoHSB(ColorLib.red(fillColor), ColorLib.green(fillColor), ColorLib.blue(fillColor), null);
		return ColorLib.getColor(ColorLib.hsb(hsv[0], hsv[1] * 0.1f, 0.9f));
	}

	private void renderImage(
		final Graphics2D g,
		final boolean useInt,
		final Image img,
		final double size,
		double x,
		double y,
		final RectangularShape shape) {
		if (img != null) {
			final double w = size * img.getWidth(null);
			final double h = size * img.getHeight(null);
			double ix = x;
			double iy = y;

			// determine one co-ordinate based on the image position
			switch (m_imagePos) {
				case Constants.LEFT:
					x += w + size * m_imageMargin;
					break;
				case Constants.RIGHT:
					ix = shape.getMaxX() - size * m_horizBorder - w;
					break;
				case Constants.TOP:
					y += h + size * m_imageMargin;
					break;
				case Constants.BOTTOM:
					iy = shape.getMaxY() - size * m_vertBorder - h;
					break;
				default:
					throw new IllegalStateException("Unrecognized image alignment setting.");
			}

			// determine the other coordinate based on image alignment
			switch (m_imagePos) {
				case Constants.LEFT:
				case Constants.RIGHT:
					// need to set image y-coordinate
					switch (m_vImageAlign) {
						case Constants.TOP:
							break;
						case Constants.BOTTOM:
							iy = shape.getMaxY() - size * m_vertBorder - h;
							break;
						case Constants.CENTER:
							iy = shape.getCenterY() - h / 2;
							break;
						default:
							break;
					}
					break;
				case Constants.TOP:
				case Constants.BOTTOM:
					// need to set image x-coordinate
					switch (m_hImageAlign) {
						case Constants.LEFT:
							break;
						case Constants.RIGHT:
							ix = shape.getMaxX() - size * m_horizBorder - w;
							break;
						case Constants.CENTER:
							ix = shape.getCenterX() - w / 2;
							break;
						default:
							break;
					}
					break;
				default:
					break;
			}

			if (useInt && size == 1.0) {
				// if possible, use integer precision
				// results in faster, flicker-free image rendering
				g.drawImage(img, (int) ix, (int) iy, null);
			}
			else {
				transform.setTransform(size, 0, 0, size, ix, iy);
				g.drawImage(img, transform, null);
			}
		}

	}

	private void drawString(
		final Graphics2D g,
		final FontMetrics fm,
		final String text,
		final boolean useInt,
		final double x,
		final double y,
		final double w) {
		// compute the x-coordinate
		double tx;
		switch (m_hTextAlign) {
			case Constants.LEFT:
				tx = x;
				break;
			case Constants.RIGHT:
				tx = x + w - fm.stringWidth(text);
				break;
			case Constants.CENTER:
				tx = x + (w - fm.stringWidth(text)) / 2;
				break;
			default:
				throw new IllegalStateException("Unrecognized text alignment setting.");
		}

		if (useInt) {
			g.drawString(text, (int) tx, (int) y);
		}
		else {
			g.drawString(text, (float) tx, (float) y);
		}
	}

}
