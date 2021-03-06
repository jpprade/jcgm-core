/*
 * Copyright (c) 2010, Swiss AviationSoftware Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Swiss AviationSoftware Ltd. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.sf.jcgm.core;

import java.awt.image.BufferedImage;
import java.io.DataInput;
import java.io.IOException;

/**
 * Tile.
 * Class=4, Element=29
 * @version $Id:  $ 
 * @author  xphc
 * @since Oct 5, 2010
 */
public class Tile extends TileElement {

	Tile(int ec, int eid, int l, DataInput in) throws IOException {
		super(ec, eid, l, in);

		this.compressionType = CompressionType.get(makeIndex());
		this.rowPaddingIndicator = makeInt();

		int cellColorPrecision = makeInt();
		if (cellColorPrecision == 0) {
			if (ColourSelectionMode.getType() == ColourSelectionMode.Type.INDEXED) {
				cellColorPrecision = ColourIndexPrecision.getPrecision();
			}
			else {
				cellColorPrecision = ColourPrecision.getPrecision();
			}
		}

		readSdrAndBitStream();
	}

	@Override
	protected void readBitmap() {
		this.bytes = readBytes();

	}

	@Override
	public void paint(CGMDisplay d) {		
		TileArrayInfo tileArrayInfo = d.getTileArrayInfo();
		int width = tileArrayInfo.getNCellsPerTileInPathDirection();
		int height = tileArrayInfo.getNCellsPerTileInLineDirection();
		if(this.bytes!=null && this.compressionType==CompressionType.BITMAP) {
			BufferedImage imageOut = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

			byte[] imgdata = this.bytes.array();

			int x = 0;
			int y = 0;
			for(int i=0;i<imgdata.length;i=i+3) {
				byte r = imgdata[i];
				byte g = imgdata[i+1];
				byte b = imgdata[i+2];
				imageOut.setRGB(x, y, (r <<16) + (g <<8) + b);			
				x=x+1;
				if(x>=width) {
					x=0;
					y++;
				}			
			}

			this.bufferedImage=imageOut;
			super.paint(d);
		}else if(this.bytes!=null && this.compressionType==CompressionType.RUN_LENGTH) {
			BufferedImage imageOut = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			byte[] imgdata = this.bytes.array();
			int x = 0;
			int y = 0;
			for(int i=0;i<imgdata.length;i=i+5) {
				int nbpixel =  (imgdata[i] <<8) + unsignedToBytes(imgdata[i+1]);
				byte r = imgdata[i+2];
				byte g = imgdata[i+3];
				byte b = imgdata[i+4];
				for(int j=0;j<nbpixel;j++) {
					imageOut.setRGB(x, y, (r <<16) + (g <<8) + b);
					x=x+1;
					if(x>=width) {
						x=0;
						y++;
					}	
				}
			}

			this.bufferedImage=imageOut;
			super.paint(d);
		} else {
			super.paint(d);
		}
	}

	public static int unsignedToBytes(byte b) {
		return b & 0xFF;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Tile [compressionType=");
		builder.append(this.compressionType);
		builder.append(", rowPaddingIndicator=");
		builder.append(this.rowPaddingIndicator);
		builder.append("]");
		return builder.toString();
	}

}
