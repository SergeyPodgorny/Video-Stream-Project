let currentPath = '';
let pathStack = [];

async function getPlaylist(path = '') {
    try {
      const url = path 
        ? `/playlist/folder?path=${encodeURIComponent(path)}`
        : '/playlist';
      
      const response = await fetch(url, { method: 'GET' });

      if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Failed to fetch playlist:', error);
      throw error;
    }
}

function getFormatBadge(format, requiresTranscoding) {
    const nativeFormats = ['MP4', 'WEBM', 'OGG'];
    const isNative = nativeFormats.includes(format.toUpperCase());

    if (isNative) {
      return `<span style="background-color: #28a745; color: white; padding: 2px 8px; border-radius: 3px; font-size: 11px;">${format}</span>`;
    } else {
      return `<span style="background-color: #ffc107; color: black; padding: 2px 8px; border-radius: 3px; font-size: 11px;">${format} → MP4</span>`;
    }
}

function navigateToFolder(folderPath, folderName) {
    pathStack.push({ path: currentPath, name: folderName });
    currentPath = folderPath;
    renderPlaylist();
}

function navigateUp() {
    if (pathStack.length > 0) {
        const prev = pathStack.pop();
        currentPath = prev.path;
        renderPlaylist();
    }
}

function renderBreadcrumbs() {
    let html = '<div style="margin-bottom: 20px; padding: 10px; background-color: #f5f5f5; border-radius: 5px;">';
    html += `<a href="#" onclick="event.preventDefault(); resetNavigation(); renderPlaylist();" style="color: #0066cc; text-decoration: none;">🏠 Главная</a>`;
    
    let accumulatedPath = '';
    pathStack.forEach((item, index) => {
        html += ` <span style="color: #666;">/</span> `;
        if (index === pathStack.length - 1) {
            html += `<span style="color: #333; font-weight: bold;">${item.name}</span>`;
        } else {
            accumulatedPath = accumulatedPath 
                ? accumulatedPath + '/' + item.name 
                : item.name;
            html += `<a href="#" onclick="event.preventDefault(); navigateToFolder('${accumulatedPath}', '${item.name}');" style="color: #0066cc; text-decoration: none;">${item.name}</a>`;
        }
    });
    
    html += '</div>';
    return html;
}

function resetNavigation() {
    currentPath = '';
    pathStack = [];
}

async function renderPlaylist() {
    try {
      const data = await getPlaylist(currentPath);
      let html = '';

      // Добавляем навигационную цепочку
      html += renderBreadcrumbs();

      // Кнопка "Наверх" если не в корне
      if (pathStack.length > 0) {
        html += `<button onclick="navigateUp()" style="margin-bottom: 15px; padding: 8px 16px; background-color: #6c757d; color: white; border: none; border-radius: 5px; cursor: pointer;">⬆️ На уровень вверх</button>`;
      }

      // Проверяем, что вернулось: папки или видео
      if (data.length === 0) {
        html = '<p>В этой папке нет видео файлов</p>';
      } else if (data[0].subFolders !== undefined) {
        // Это папки - отображаем их
        html += '<div class="folders-grid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 15px;">';
        
        data.forEach(folder => {
          const folderIcon = folder.folderCount > 0 ? '📁' : '📂';
          const hasVideosBadge = folder.hasVideos 
            ? '<span style="font-size: 12px; color: #28a745;">✓ видео</span>' 
            : '';
          
          html += `
            <div class="folder-card" 
                 onclick="navigateToFolder('${folder.relativePath}', '${folder.name}')"
                 style="background-color: #f8f9fa; border: 1px solid #dee2e6; border-radius: 8px; padding: 15px; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s;"
                 onmouseover="this.style.transform='translateY(-2px)'; this.style.boxShadow='0 4px 8px rgba(0,0,0,0.1)';"
                 onmouseout="this.style.transform='translateY(0)'; this.style.boxShadow='none';">
              <div style="font-size: 48px; text-align: center; margin-bottom: 10px;">${folderIcon}</div>
              <div style="font-weight: bold; text-align: center; margin-bottom: 5px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${folder.name}</div>
              <div style="text-align: center; font-size: 12px; color: #666;">
                ${folder.folderCount > 0 ? `📁 ${folder.folderCount} папок` : ''}
                ${folder.folderCount > 0 && folder.hasVideos ? '<br/>' : ''}
                ${hasVideosBadge}
              </div>
            </div>`;
        });
        
        html += '</div>';
      } else {
        // Это видео - отображаем таблицу
        html += '<table style="border-collapse: collapse; width: 100%;">';
        html += `
          <thead>
            <tr style="background-color: #f0f0f0;">
              <th style="padding: 10px; text-align: left;">Файл</th>
              <th style="padding: 10px;">Формат</th>
            </tr>
          </thead>
          <tbody>`;

        data.forEach(video => {
          const transcodingHint = video.requiresTranscoding
            ? ' (требуется транскодировка)'
            : '';
          const rowStyle = video.requiresTranscoding
            ? 'style="background-color: #fffde7;"'
            : '';

          html += `
            <tr ${rowStyle} style="border-bottom: 1px solid #ddd;">
              <td style="padding: 10px;">
                <a href="${video.url}" style="text-decoration: none; color: #0066cc;">${video.title}</a>
                <span style="color: #666; font-size: 12px; margin-left: 8px;">${transcodingHint}</span>
              </td>
              <td style="padding: 10px; text-align: center;">
                ${getFormatBadge(video.format, video.requiresTranscoding)}
              </td>
            </tr>`;
        });

        html += `
          </tbody>
        </table>`;
      }

      const container = document.getElementById('container');
      container.innerHTML = html;
    } catch (error) {
      const container = document.getElementById('container');
      container.innerHTML = '<p style="color: red;">Ошибка загрузки плейлиста</p>';
      console.error('Render error:', error);
    }
}

// Инициализация при загрузке
renderPlaylist();