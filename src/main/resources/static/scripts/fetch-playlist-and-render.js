async function getPlaylist() {
    try {
      const response = await fetch(
        '/playlist',
        {
          method: 'GET',
        },
      );
  
      if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
      }
  
      const data = await response.json();
      return data;
    } catch (error) {
      console.log(error);
    }
  }
  
  getPlaylist().then(data => {
  
  
    let table = '<table style="border-collapse: collapse;">';
  
  table += `
    <thead>
      <tr>
        <th>Доступные файлы</th>
      </tr>
    </thead>
  
    <tbody>`;
  
  Object.keys(data).forEach(data => {
    table += `
      <tr>
        <td><a href="streamer_page.html?video=${data}">${data}</a></td>
      </tr>`;
  });
  
  table += `
    </tbody>
  </table>`;
  
    const container = document.getElementById('container');
    container.innerHTML = table;
      
  });